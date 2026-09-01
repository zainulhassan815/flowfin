package com.flowfin.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountFlow
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Transaction
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.colorKey
import com.flowfin.core.ui.dateLabel
import com.flowfin.core.ui.iconKey
import com.flowfin.core.ui.monthShortLabel
import com.flowfin.core.ui.parentName
import com.flowfin.core.ui.toRowUi
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

/**
 * Drives Account detail: the account's hero (name, type, balance), this month's
 * In / Out / Net strip, and its full ledger newest-first. Everything is computed
 * on read — the balance and the flow are queries, not stored counters.
 *
 * "Now" is sampled once at construction so the month window and the Today /
 * Yesterday labels stay stable for the ViewModel's life.
 */
class AccountDetailViewModel(
  private val accountId: AccountId,
  accounts: AccountRepository,
  transactions: TransactionRepository,
  categories: CategoryRepository,
  debts: DebtRepository,
  clock: Clock,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val now = clock.now()
  private val zone = TimeZone.currentSystemDefault()
  private val today = now.toLocalDateTime(zone).date
  private val monthStart = LocalDate(today.year, today.month, 1).atStartOfDayIn(zone)
  private val monthEnd = LocalDate(today.year, today.month, 1).plus(1, DateTimeUnit.MONTH).atStartOfDayIn(zone)
  private val monthLabel = monthShortLabel(today)

  val uiState: StateFlow<AccountDetailUiState> = combine(
    accounts.observeBalances(),
    categories.observeAll(),
    transactions.observeByAccount(accountId, ACTIVITY_LIMIT, offset = 0),
    transactions.observeFlow(accountId, monthStart, monthEnd),
    debts.observePersonNames(),
  ) { balances, categoryList, feed, flow, debtPersons ->
    buildState(balances, categoryList, feed, flow, debtPersons)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AccountDetailUiState.Loading)

  private fun buildState(
    balances: List<AccountBalance>,
    categoryList: List<Category>,
    feed: List<Transaction>,
    flow: AccountFlow,
    debtPersons: Map<DebtId, String>,
  ): AccountDetailUiState {
    val self = balances.firstOrNull { it.account.id == accountId } ?: return AccountDetailUiState.NotFound
    val account = self.account
    val accountsById = balances.associate { it.account.id to it.account }
    val categoriesById = categoryList.associateBy(Category::id)

    return AccountDetailUiState.Content(
      name = account.name,
      typeLabel = UiText.Res(if (account.isBudget) R.string.account_detail_type_budget else R.string.account_detail_type_real),
      typeTag = UiText.Res(if (account.isBudget) R.string.account_detail_tag_budget else R.string.account_detail_tag_real),
      meta = account.parentName(accountsById),
      iconKey = account.iconKey(),
      colorKey = account.colorKey(),
      isBudget = account.isBudget,
      currency = money.symbol,
      balanceWhole = money.whole(self.balance),
      balanceDecimal = money.fraction(self.balance),
      noActivity = feed.isEmpty(),
      flow = flow.toUi(),
      activity = feed.groupActivity(accountsById, categoriesById, debtPersons),
    )
  }

  /** The strip drops entirely when nothing moved this month — there's nothing to
   *  summarise, and a row of zeros reads as noise. */
  private fun AccountFlow.toUi(): FlowUi? {
    if (inflow.isZero && outflow.isZero) return null
    val sign = when {
      net.isPositive -> FlowSign.Up
      net.isNegative -> FlowSign.Down
      else -> FlowSign.Flat
    }
    // money.whole already prefixes '−' for negatives; only the positive case needs a sign.
    val netText = (if (net.isPositive) "+" else "") + money.whole(net)
    return FlowUi(month = monthLabel, inflow = money.whole(inflow), outflow = money.whole(outflow), net = netText, netSign = sign)
  }

  private fun List<Transaction>.groupActivity(
    accountsById: Map<AccountId, Account>,
    categoriesById: Map<CategoryId, Category>,
    debtPersons: Map<DebtId, String>,
  ): List<TxGroup> =
    groupBy { it.recordedAt.toLocalDateTime(zone).date } // newest-first feed → dates stay descending
      .map { (date, txns) ->
        TxGroup(dateLabel(date, today), txns.map { it.toRowUi(accountsById, categoriesById, money, accountId, debtPersons) })
      }
}

/** A single generous page of history — real pagination arrives only if a benchmark
 *  shows an account's ledger outgrowing it. */
private const val ACTIVITY_LIMIT = 100L
private const val STOP_TIMEOUT_MS = 5_000L
