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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
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

  private val now = clock.now()
  private val zone = TimeZone.currentSystemDefault()
  private val today = now.toLocalDateTime(zone).date
  private val monthStart = LocalDate(today.year, today.month, 1).atStartOfDayIn(zone)
  private val monthEnd = LocalDate(today.year, today.month, 1).plus(1, DateTimeUnit.MONTH).atStartOfDayIn(zone)
  private val weekStart = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

  val uiState: StateFlow<HomeUiState> = combine(
    accounts.observeBalances(),
    transactions.recentFeed(RECENT_LIMIT),
    categories.observeAll(),
    recurring.observePending(now),
    transactions.observeNetChange(monthStart, monthEnd),
  ) { balances, recent, categoryList, pending, net ->
    buildState(balances, recent, categoryList, pending, net)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)

  private fun buildState(
    balances: List<AccountBalance>,
    recent: List<Transaction>,
    categoryList: List<Category>,
    pending: List<RecurringSchedule>,
    net: Money,
  ): HomeUiState {
    if (balances.isEmpty()) return HomeUiState.Empty

    val accountsById = balances.associate { it.account.id to it.account }
    val categoriesById = categoryList.associateBy(Category::id)

    val real = balances.filter { it.account.isReal }
    val budget = balances.filter { it.account.isBudget }
    val realSum = real.fold(Money.ZERO) { sum, b -> sum + b.balance }
    val budgetSum = budget.fold(Money.ZERO) { sum, b -> sum + b.balance }
    val total = realSum + budgetSum // the headline total is exactly the sum of every account

    return HomeUiState.Content(
      totalWhole = money.whole(total),
      totalDecimal = money.fraction(total),
      allocated = money.displayWhole(budgetSum),
      aside = heroAside(total, net, daysSinceStart(balances)),
      realTotal = money.displayWhole(realSum),
      realAccounts = real.map { it.toCardUi(accountsById) },
      budgetTotal = money.displayWhole(budgetSum),
      budgetAccounts = budget.map { it.toCardUi(accountsById) },
      pending = pending.take(PENDING_LIMIT).map { it.toPendingUi(accountsById) },
      pendingTotal = pending.size,
      recent = classifyRecent(recent, accountsById, categoriesById),
    )
  }

  /** Home shows this week's activity. With nothing this week it asks which empty
   *  it is: nothing ever logged ([RecentSection.NoEntries]) or just a lull
   *  ([RecentSection.Quiet], dated off the newest entry). */
  private fun classifyRecent(
    recent: List<Transaction>,
    accountsById: Map<AccountId, Account>,
    categoriesById: Map<CategoryId, Category>,
  ): RecentSection {
    val thisWeek = recent.filter { it.recordedAt.toLocalDateTime(zone).date >= weekStart }
    return when {
      thisWeek.isNotEmpty() -> RecentSection.Activity(groupRecent(thisWeek, accountsById, categoriesById))
      recent.isEmpty() -> RecentSection.NoEntries
      else -> RecentSection.Quiet(lastEntry = daysAgo(recent.first().recordedAt))
    }
  }

  private fun daysAgo(instant: Instant): String =
    when (val days = today.toEpochDays() - instant.toLocalDateTime(zone).date.toEpochDays()) {
      0 -> "today"
      1 -> "1 day ago"
      else -> "$days days ago"
    }

  /** Days the user has been tracking, 1-indexed from the oldest account (its
   *  creation day is "Day 1"). */
  private fun daysSinceStart(balances: List<AccountBalance>): Int {
    val firstDay = balances.minOf { it.account.createdAt }.toLocalDateTime(zone).date
    return (today.toEpochDays() - firstDay.toEpochDays() + 1).coerceAtLeast(1)
  }

  /** Before there's enough history to compare months, the hero owns its early days
   *  ("Day 5, building a picture"); after that it shows the month delta. */
  private fun heroAside(total: Money, net: Money, daysSinceStart: Int): HeroAside? =
    if (daysSinceStart < SETTLING_DAYS) HeroAside.Settling("Day $daysSinceStart") else trendOf(total, net)

  /** A delta vs the month's starting total, or null when there's nothing honest to
   *  show (no change, or the month opened at zero so a percentage is meaningless). */
  private fun trendOf(total: Money, net: Money): HeroAside.Trend? {
    if (net.isZero) return null
    val startTotal = total.minorUnits - net.minorUnits
    if (startTotal <= 0L) return null
    val pct = (net.minorUnits.toDouble() / startTotal.toDouble() * 100).roundToInt()
    if (pct == 0) return null
    val sign = if (pct > 0) "+" else "" // a negative pct already carries its '−'
    return HeroAside.Trend(percent = "$sign$pct%", rising = net.isPositive)
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
      append(money.displayWhole(amount))
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
}

/** "MONDAY" → "Mon", "DECEMBER" → "Dec". */
private fun String.titleCase3(): String =
  take(3).lowercase().replaceFirstChar { it.uppercase() }

private const val RECENT_LIMIT = 20L
private const val STOP_TIMEOUT_MS = 5_000L

/** Home surfaces only the most pressing pending payments; the count badge keeps
 *  the true total and "All" opens the rest. */
private const val PENDING_LIMIT = 3

/** Below this many days of history, the hero reads "Day N" instead of a month
 *  trend — a month-over-month delta isn't meaningful in the first few weeks. */
private const val SETTLING_DAYS = 28
