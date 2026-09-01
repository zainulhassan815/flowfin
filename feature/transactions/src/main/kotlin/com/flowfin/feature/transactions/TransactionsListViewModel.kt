package com.flowfin.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Money
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.dateLabel
import com.flowfin.core.ui.rowKind
import com.flowfin.core.ui.toRowUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Drives the full transactions ledger: the whole feed newest-first, grouped by
 * month then day, with a per-month In − Out net and an All / In / Out /
 * Transfers filter. Everything is computed on read — the feed is a query and the
 * monthly nets are summed in memory; no stored aggregates.
 *
 * "Now" is sampled once at construction so the Today / Yesterday labels stay
 * stable for the ViewModel's life. The feed is capped at [FEED_LIMIT]; real
 * pagination waits for a benchmark that shows a ledger outgrowing it.
 */
class TransactionsListViewModel(
  transactions: TransactionRepository,
  accounts: AccountRepository,
  categories: CategoryRepository,
  debts: DebtRepository,
  clock: Clock,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val zone = TimeZone.currentSystemDefault()
  private val today = clock.now().toLocalDateTime(zone).date

  private val filter = MutableStateFlow(TxFilter.All)

  fun setFilter(value: TxFilter) {
    filter.value = value
  }

  // Filtering happens at the query level: the selected filter swaps the feed
  // source (its kinds, or the whole ledger). The filter is paired with the rows
  // it produced in one emission, so the chips and the list can never disagree.
  @OptIn(ExperimentalCoroutinesApi::class)
  val uiState: StateFlow<TransactionsListUiState> = combine(
    filter.flatMapLatest { active ->
      val source = active.kinds()?.let { transactions.feedOfKinds(it, FEED_LIMIT) }
        ?: transactions.feed(FEED_LIMIT)
      source.map { feed -> active to feed }
    },
    accounts.observeActiveAccounts(),
    categories.observeAll(),
    debts.observePersonNames(),
  ) { (active, feed), accountList, categoryList, debtPersons ->
    buildState(feed, accountList, categoryList, debtPersons, active)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TransactionsListUiState.Loading)

  private fun buildState(
    feed: List<Transaction>,
    accountList: List<Account>,
    categoryList: List<Category>,
    debtPersons: Map<DebtId, String>,
    active: TxFilter,
  ): TransactionsListUiState {
    if (feed.isEmpty()) {
      // The feed is pre-filtered, so an empty result is ambiguous: under All it's an
      // empty ledger (first-run takeover); under a filter it's an empty slice (the
      // screen shows the inline "no <kind>" hint, keeping the chips live).
      return if (active == TxFilter.All) TransactionsListUiState.Empty
      else TransactionsListUiState.Content(active, emptyList())
    }

    val accountsById = accountList.associateBy(Account::id)
    val categoriesById = categoryList.associateBy(Category::id)

    // The feed is already filtered at the query level; just join and group here.
    val items = feed.map { tx ->
      Item(
        date = tx.recordedAt.toLocalDateTime(zone).date,
        kind = tx.kind,
        amount = tx.amount,
        row = tx.toRowUi(accountsById, categoriesById, money, debtPersons = debtPersons),
      )
    }

    // The feed is newest-first, so groupBy preserves month- and day-descending order.
    val months = items
      .groupBy { monthId(it.date) }
      .map { (id, monthItems) ->
        val net = externalNet(monthItems)
        // The net is a whole-month figure, so it's only honest on the unfiltered view;
        // a month of internal moves only (transfers / allocations) has none to show.
        val showNet = active == TxFilter.All && !net.isZero
        MonthSection(
          id = id,
          label = monthLabel(monthItems.first().date),
          currency = money.symbol,
          net = if (showNet) signedWhole(net) else null,
          netPositive = net.isPositive,
          days = monthItems
            .groupBy { it.date }
            .map { (date, dayItems) ->
              DayGroup(
                dateLabel = dateLabel(date, today),
                isToday = date == today,
                rows = dayItems.map { it.row },
              )
            },
        )
      }

    return TransactionsListUiState.Content(filter = active, months = months)
  }

  /** The domain kinds a filter queries, derived from its display [TxFilter.bucket]
   *  via the shared [rowKind] mapping; null (All) reads the whole ledger. */
  private fun TxFilter.kinds(): Set<TransactionKind>? =
    bucket?.let { b -> TransactionKind.entries.filterTo(mutableSetOf()) { it.rowKind() == b } }

  /** This month's headline movement: external inflows minus outflows; internal
   *  moves (transfers / allocations) don't count. Mirrors `observeNetChange`. */
  private fun externalNet(items: List<Item>): Money =
    items.fold(Money.ZERO) { acc, item ->
      when (item.kind.rowKind()) {
        RowKind.Income -> acc + item.amount
        RowKind.Expense -> acc - item.amount
        RowKind.Transfer -> acc
      }
    }

  /** `money.whole` already prefixes '−' for negatives; only positives need a '+'. */
  private fun signedWhole(net: Money): String = (if (net.isPositive) "+" else "") + money.whole(net)

  /** A stable per-month grouping key — never shown, so a plain join is enough. */
  private fun monthId(date: LocalDate): String = "${date.year}-${date.monthNumber}"

  /** "December 2026" — month name is the English enum form (full date localization
   *  is a separate effort, as in `dateLabel`); the year is joined via a resource. */
  private fun monthLabel(date: LocalDate): UiText {
    val name = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return UiText.Res(R.string.transactions_month_label, listOf(name, date.year))
  }
}

/** A joined ledger entry: its day, domain kind/amount (for the net) and display row. */
private data class Item(
  val date: LocalDate,
  val kind: TransactionKind,
  val amount: Money,
  val row: TxRowUi,
)

/** A generous single page of history; pagination arrives only if a benchmark needs it. */
private const val FEED_LIMIT = 200L
private const val STOP_TIMEOUT_MS = 5_000L
