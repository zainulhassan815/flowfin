package com.flowfin.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.usecase.FireSchedule
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Money
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.model.Transaction
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.dateLabel
import com.flowfin.core.ui.toCardUi
import com.flowfin.core.ui.toRowUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
  debts: DebtRepository,
  private val fireSchedule: FireSchedule,
  clock: Clock,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val now = clock.now()
  private val zone = TimeZone.currentSystemDefault()
  private val today = now.toLocalDateTime(zone).date
  private val monthStart = LocalDate(today.year, today.month, 1).atStartOfDayIn(zone)
  private val monthEnd = LocalDate(today.year, today.month, 1).plus(1, DateTimeUnit.MONTH).atStartOfDayIn(zone)
  private val weekStart = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

  /** The two pure lookups a feed row needs, paired so the state combine below
   *  stays inside `combine`'s five-flow arity. */
  private val lookups = combine(categories.observeAll(), debts.observePersonNames(), ::Lookups)

  val uiState: StateFlow<HomeUiState> = combine(
    accounts.observeBalances(),
    transactions.feed(RECENT_LIMIT),
    lookups,
    recurring.observePending(now),
    transactions.observeNetChange(monthStart, monthEnd),
  ) { balances, recent, lookup, pending, net ->
    buildState(balances, recent, lookup, pending, net)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)

  private data class Lookups(val categories: List<Category>, val debtPersons: Map<DebtId, String>)

  private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  /** Guards a double-tap from firing the same schedule twice before the reactive
   *  list drops its row. Touched only on the main thread, so a plain set is enough. */
  private val inFlight = mutableSetOf<RecurringScheduleId>()

  /**
   * Record the pending payment and advance its due date — the row leaves the
   * Pending strip on the next emission. Mirrors the Recurring tab's Mark paid; the
   * two surfaces show the same schedules and have to do the same thing to them.
   * [name] is only for the confirmation message.
   */
  fun payPending(id: RecurringScheduleId, name: String) {
    if (!inFlight.add(id)) return
    viewModelScope.launch {
      try {
        val effect = when (fireSchedule(id)) {
          is Either.Right -> HomeEffect.ShowMessage(UiText.Res(R.string.recurring_paid_confirm, listOf(name)))
          is Either.Left -> HomeEffect.ShowMessage(UiText.Res(R.string.recurring_action_error))
        }
        effectChannel.send(effect)
      } finally {
        inFlight.remove(id)
      }
    }
  }

  private fun buildState(
    balances: List<AccountBalance>,
    recent: List<Transaction>,
    lookup: Lookups,
    pending: List<RecurringSchedule>,
    net: Money,
  ): HomeUiState {
    if (balances.isEmpty()) return HomeUiState.Empty(money.symbol)

    val accountsById = balances.associate { it.account.id to it.account }
    val categoriesById = lookup.categories.associateBy(Category::id)

    val real = balances.filter { it.account.isReal }
    val budget = balances.filter { it.account.isBudget }
    val realSum = real.fold(Money.ZERO) { sum, b -> sum + b.balance }
    val budgetSum = budget.fold(Money.ZERO) { sum, b -> sum + b.balance }
    val total = realSum + budgetSum // the headline total is exactly the sum of every account

    return HomeUiState.Content(
      currency = money.symbol,
      totalWhole = money.whole(total),
      totalDecimal = money.fraction(total),
      allocated = money.displayWhole(budgetSum),
      aside = heroAside(total, net, daysSinceStart(balances)),
      realTotal = money.displayWhole(realSum),
      realAccounts = real.map { it.toCardUi(accountsById, money) },
      budgetTotal = money.displayWhole(budgetSum),
      budgetAccounts = budget.map { it.toCardUi(accountsById, money) },
      pending = pending.take(PENDING_LIMIT).map { it.toPendingUi(accountsById) },
      pendingTotal = pending.size,
      recent = classifyRecent(recent, accountsById, categoriesById, lookup.debtPersons),
    )
  }

  /** Home shows this week's activity. With nothing this week it asks which empty
   *  it is: nothing ever logged ([RecentSection.NoEntries]) or just a lull
   *  ([RecentSection.Quiet], dated off the newest entry). */
  private fun classifyRecent(
    recent: List<Transaction>,
    accountsById: Map<AccountId, Account>,
    categoriesById: Map<CategoryId, Category>,
    debtPersons: Map<DebtId, String>,
  ): RecentSection {
    val thisWeek = recent.filter { it.recordedAt.toLocalDateTime(zone).date >= weekStart }
    return when {
      thisWeek.isNotEmpty() -> RecentSection.Activity(groupRecent(thisWeek, accountsById, categoriesById, debtPersons))
      recent.isEmpty() -> RecentSection.NoEntries
      else -> RecentSection.Quiet(lastEntry = daysAgo(recent.first().recordedAt))
    }
  }

  private fun daysAgo(instant: Instant): UiText {
    val days = today.toEpochDays() - instant.toLocalDateTime(zone).date.toEpochDays()
    return if (days == 0) UiText.Res(R.string.home_days_ago_today) else UiText.Plural(R.plurals.home_days_ago, days)
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
    if (daysSinceStart < SETTLING_DAYS) {
      HeroAside.Settling(UiText.Res(R.string.home_hero_day, listOf(daysSinceStart)))
    } else {
      trendOf(total, net)
    }

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

  private fun RecurringSchedule.toPendingUi(accountsById: Map<AccountId, Account>): PendingRowUi {
    val accountId = toAccountId ?: fromAccountId
    val accountName = accountId?.let { accountsById[it]?.name }.orEmpty()
    val daysLate = today.toEpochDays() - nextDueAt.toLocalDateTime(zone).date.toEpochDays()
    val (statusText, urgency) = if (daysLate <= 0) {
      UiText.Res(R.string.home_pending_due_today) to PendingUrgency.Due
    } else {
      UiText.Plural(R.plurals.home_pending_days_late, daysLate) to PendingUrgency.Late
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
    debtPersons: Map<DebtId, String>,
  ): List<RecentGroup> =
    recent
      .groupBy { it.recordedAt.toLocalDateTime(zone).date } // newest-first feed → dates stay descending
      .map { (date, txns) ->
        RecentGroup(
          dateLabel = dateLabel(date, today),
          rows = txns.map { it.toRowUi(accountsById, categoriesById, money, debtPersons = debtPersons) },
        )
      }
}

private const val RECENT_LIMIT = 20L
private const val STOP_TIMEOUT_MS = 5_000L

/** Home surfaces only the most pressing pending payments; the count badge keeps
 *  the true total and "All" opens the rest. */
private const val PENDING_LIMIT = 3

/** Below this many days of history, the hero reads "Day N" instead of a month
 *  trend — a month-over-month delta isn't meaningful in the first few weeks. */
private const val SETTLING_DAYS = 28
