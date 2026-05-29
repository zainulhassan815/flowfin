package com.flowfin.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.Money
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.TxRowUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

/**
 * Drives Home from the domain: the headline total and its "this month" trend, the
 * allocated sum, account cards split into Real / Budget segments, the due/overdue
 * recurring payments, and a hydrated, date-grouped recent feed — the feed is joined
 * in-memory against accounts (for names) and categories (names/icons/colours).
 *
 * "Now" is sampled once at construction: pending and the date labels (Today /
 * Yesterday) are anchored to that instant, recomputed whenever the ViewModel is.
 */
class HomeViewModel(
  accounts: AccountRepository,
  transactions: TransactionRepository,
  categories: CategoryRepository,
  recurring: RecurringRepository,
  clock: Clock,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val zone = TimeZone.currentSystemDefault()
  private val today = clock.now().toLocalDateTime(zone).date
  private val monthStart = LocalDate(today.year, today.month, 1).atStartOfDayIn(zone)
  private val monthEnd = LocalDate(today.year, today.month, 1).plus(1, DateTimeUnit.MONTH).atStartOfDayIn(zone)

  val uiState: StateFlow<HomeUiState> = combine(
    accounts.observeBalances(),
    accounts.observeTotalBalance(),
    transactions.recentFeed(RECENT_LIMIT),
    categories.observeAll(),
    recurring.observePending(clock.now()),
  ) { balances, total, recent, categoryList, pending ->
    Inputs(balances, total, recent, categoryList, pending)
  }.combine(transactions.observeNetChange(monthStart, monthEnd)) { inputs, net ->
    inputs.toState(net)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)

  /** The five account/feed streams, bundled so a sixth (net change) can join them. */
  private data class Inputs(
    val balances: List<AccountBalance>,
    val total: Money,
    val recent: List<Transaction>,
    val categories: List<Category>,
    val pending: List<RecurringSchedule>,
  )

  private fun Inputs.toState(net: Money): HomeUiState {
    if (balances.isEmpty()) return HomeUiState.Empty

    val accountsById = balances.associate { it.account.id to it.account }
    val categoriesById = categories.associateBy(Category::id)

    val real = balances.filter { it.account.isReal }
    val budget = balances.filter { it.account.isBudget }
    val budgetSum = budget.fold(Money.ZERO) { sum, b -> sum + b.balance }

    return HomeUiState.Content(
      totalWhole = money.whole(total),
      totalDecimal = money.fraction(total),
      allocated = plain(budgetSum),
      trend = trendOf(total, net),
      realTotal = plain(real.fold(Money.ZERO) { sum, b -> sum + b.balance }),
      realAccounts = real.map { it.toCardUi(accountsById) },
      budgetTotal = plain(budgetSum),
      budgetAccounts = budget.map { it.toCardUi(accountsById) },
      pending = pending.map { it.toPendingUi(accountsById) },
      recent = groupRecent(recent, accountsById, categoriesById),
    )
  }

  /** A delta vs the month's starting total, or null when there's nothing honest to
   *  show (no change, or the month opened at zero so a percentage is meaningless). */
  private fun trendOf(total: Money, net: Money): HomeTrend? {
    if (net.isZero) return null
    val startTotal = total.minorUnits - net.minorUnits
    if (startTotal <= 0L) return null
    val pct = (net.minorUnits.toDouble() / startTotal.toDouble() * 100).roundToInt()
    if (pct == 0) return null
    val sign = if (pct > 0) "+" else "" // a negative pct already carries its '−'
    return HomeTrend(percent = "$sign$pct%", rising = net.isPositive)
  }

  private fun AccountBalance.toCardUi(accountsById: Map<AccountId, Account>): AccountCardUi {
    val meta = if (account.isBudget) {
      account.parentAccountId?.let { accountsById[it]?.name }.orEmpty()
    } else {
      ""
    }
    return AccountCardUi(
      id = account.id,
      name = account.name,
      meta = meta,
      balanceWhole = money.whole(balance),
      balanceDecimal = money.fraction(balance),
      iconKey = account.icon ?: if (account.isBudget) "wallet" else "bank",
      colorKey = account.color ?: if (account.isBudget) "other" else "bank",
      isBudget = account.isBudget,
    )
  }

  private fun RecurringSchedule.toPendingUi(accountsById: Map<AccountId, Account>): PendingRowUi {
    val accountId = toAccountId ?: fromAccountId
    val accountName = accountId?.let { accountsById[it]?.name }.orEmpty()
    val daysLate = today.toEpochDays() - nextDueAt.toLocalDateTime(zone).date.toEpochDays()
    val (statusText, urgency) = when {
      daysLate <= 0 -> "Due today" to PendingUrgency.Due
      daysLate == 1 -> "1 day late" to PendingUrgency.Late
      else -> "$daysLate days late" to PendingUrgency.Late
    }
    val amountAccount = buildString {
      append(plain(amount))
      if (accountName.isNotEmpty()) append(" · ").append(accountName)
    }
    return PendingRowUi(id, name, amountAccount, statusText, urgency)
  }

  private fun groupRecent(
    recent: List<Transaction>,
    accountsById: Map<AccountId, Account>,
    categoriesById: Map<CategoryId, Category>,
  ): List<RecentGroup> =
    recent
      .groupBy { it.recordedAt.toLocalDateTime(zone).date } // newest-first feed → dates stay descending
      .map { (date, txns) ->
        RecentGroup(
          dateLabel = dateLabel(date),
          rows = txns.map { it.toRowUi(accountsById, categoriesById) },
        )
      }

  private fun dateLabel(date: LocalDate): String {
    val prefix = when (date.toEpochDays()) {
      today.toEpochDays() -> "Today"
      today.toEpochDays() - 1 -> "Yesterday"
      else -> date.dayOfWeek.name.titleCase3()
    }
    return "$prefix · ${date.dayOfMonth} ${date.month.name.titleCase3()}"
  }

  private fun Transaction.toRowUi(
    accountsById: Map<AccountId, Account>,
    categoriesById: Map<CategoryId, Category>,
  ): TxRowUi {
    val category = categoryId?.let { categoriesById[it] }
    val fromName = fromAccountId?.let { accountsById[it]?.name }.orEmpty()
    val toName = toAccountId?.let { accountsById[it]?.name }.orEmpty()
    val moneyIn = kind == TransactionKind.INCOME ||
      kind == TransactionKind.DEBT_BORROW ||
      kind == TransactionKind.DEBT_REPAY_IN
    return TxRowUi(
      id = id,
      name = when (kind) {
        TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.name ?: "—"
        TransactionKind.TRANSFER -> "Transfer"
        TransactionKind.ALLOCATION -> "Allocation"
        TransactionKind.REALLOCATION -> "Reallocation"
        TransactionKind.DEBT_BORROW, TransactionKind.DEBT_LEND,
        TransactionKind.DEBT_REPAY_OUT, TransactionKind.DEBT_REPAY_IN -> "Debt"
      },
      meta = when (kind) {
        TransactionKind.INCOME -> toName
        TransactionKind.EXPENSE -> fromName
        TransactionKind.TRANSFER, TransactionKind.ALLOCATION, TransactionKind.REALLOCATION ->
          "$fromName → $toName"
        else -> ""
      },
      amount = (if (moneyIn) "+" else "−") + money.whole(amount),
      decimal = money.fraction(amount),
      kind = when (kind) {
        TransactionKind.INCOME, TransactionKind.DEBT_BORROW, TransactionKind.DEBT_REPAY_IN -> RowKind.Income
        TransactionKind.EXPENSE, TransactionKind.DEBT_LEND, TransactionKind.DEBT_REPAY_OUT -> RowKind.Expense
        TransactionKind.TRANSFER, TransactionKind.ALLOCATION, TransactionKind.REALLOCATION -> RowKind.Transfer
      },
      iconKey = when (kind) {
        TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.icon
        else -> "sync_alt"
      },
      colorKey = when (kind) {
        TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.color
        else -> null
      },
    )
  }

  /** Like [MoneyFormatter.display] but without the fraction: "Rs 16,000". */
  private fun plain(amount: Money): String = "${money.symbol} ${money.whole(amount)}"
}

/** "MONDAY" → "Mon", "DECEMBER" → "Dec". */
private fun String.titleCase3(): String =
  take(3).lowercase().replaceFirstChar { it.uppercase() }

private const val RECENT_LIMIT = 20L
private const val STOP_TIMEOUT_MS = 5_000L
