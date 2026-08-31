package com.flowfin.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryTotal
import com.flowfin.core.model.DatedAmount
import com.flowfin.core.model.Money
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Reports for one month at a time. The month is the only thing the user drives;
 * everything else is derived from it, so changing month re-queries rather than
 * filtering something already loaded.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(
  private val transactions: TransactionRepository,
  private val categories: CategoryRepository,
  private val money: MoneyFormatter,
  clock: Clock,
  private val zone: TimeZone,
) : ViewModel() {

  private val today = clock.now().toLocalDateTime(zone).date
  private val thisMonth = today.firstOfMonth()

  private val selection = MutableStateFlow(Selection(thisMonth, ReportScope.EXPENSE))

  /** Whether anything has ever been recorded, which is a different question
   *  from whether the selected month has anything in it. */
  private val hasHistory = transactions.feed(limit = 1).map { it.isNotEmpty() }

  val uiState: StateFlow<ReportsUiState> = selection
    .flatMapLatest { current ->
      val start = current.month.atStartOfDayIn(zone)
      val end = current.month.plus(DatePeriod(months = 1)).atStartOfDayIn(zone)
      combine(
        transactions.observeAmountsOfKind(current.kind(), start, end),
        transactions.observeCategoryTotals(current.kind(), start, end),
        transactions.observeAmountsOfKind(TransactionKind.INCOME, start, end),
        transactions.observeAmountsOfKind(TransactionKind.EXPENSE, start, end),
        categories.observeAll(),
      ) { scoped, totals, incomes, expenses, allCategories ->
        Month(current, scoped, totals, incomes, expenses, allCategories)
      }
    }
    .combine(hasHistory) { month, everRecorded -> build(month, everRecorded) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ReportsUiState.Loading)

  fun onPreviousMonth() = selection.update { it.copy(month = it.month.minus(DatePeriod(months = 1))) }

  fun onNextMonth() = selection.update {
    // Never past the current month — there is nothing recorded ahead of today.
    if (it.month < thisMonth) it.copy(month = it.month.plus(DatePeriod(months = 1))) else it
  }

  fun onSelectScope(scope: ReportScope) = selection.update { it.copy(scope = scope) }

  private fun build(month: Month, everRecorded: Boolean): ReportsUiState {
    val (current, scoped, totals, incomes, expenses, allCategories) = month
    val income = incomes.fold(Money.ZERO) { acc, it -> acc + it.amount }
    val expense = expenses.fold(Money.ZERO) { acc, it -> acc + it.amount }
    // The takeover is only right when there is nothing at all to report on.
    // A quiet month with history behind it keeps its chrome — otherwise the
    // month strip disappears and there is no way back to the months that do
    // have data, which is every user's view on the first of the month.
    if (!everRecorded) return ReportsUiState.Empty

    val net = income - expense
    val byId = allCategories.associateBy { it.id }
    val scopeTotal = totals.fold(Money.ZERO) { acc, it -> acc + it.total }

    return ReportsUiState.Content(
      monthLabel = current.month.month.name.titleCase(),
      yearLabel = current.month.year.toString(),
      canGoForward = current.month < thisMonth,
      scope = current.scope,
      netWhole = (if (net.isNegative) "" else "+") + money.whole(net),
      netDecimal = money.fraction(net),
      netIsPositive = !net.isNegative,
      incomeTotal = money.display(income),
      expenseTotal = money.display(expense),
      trend = scoped.takeIf { it.isNotEmpty() }?.let { trend(current, it) },
      breakdown = totals.mapNotNull { total ->
        val category = byId[total.categoryId] ?: return@mapNotNull null
        BreakdownRowUi(
          name = category.name,
          colorKey = category.color,
          amountWhole = money.whole(total.total),
          amountDecimal = money.fraction(total.total),
          percent = if (scopeTotal.minorUnits > 0) {
            ((total.total.minorUnits * 100) / scopeTotal.minorUnits).toInt()
          } else {
            0
          },
          transactionCount = total.transactionCount,
          value = total.total.minorUnits.toFloat(),
        )
      },
      breakdownTotal = money.compact(scopeTotal),
      breakdownCount = totals.sumOf { it.transactionCount },
    )
  }

  private fun trend(current: Selection, amounts: List<DatedAmount>): TrendUi {
    val daysInMonth = current.month.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).dayOfMonth
    val perDay = LongArray(daysInMonth)
    amounts.forEach { entry ->
      val day = entry.recordedAt.toLocalDateTime(zone).date.dayOfMonth
      if (day in 1..daysInMonth) perDay[day - 1] += entry.amount.minorUnits
    }

    val isCurrentMonth = current.month == thisMonth
    val elapsed = if (isCurrentMonth) today.dayOfMonth else daysInMonth
    val total = perDay.sum()
    val pace = if (elapsed > 0) total.toFloat() / elapsed else 0f
    // FlowFinTrendChart takes 0..1 and clamps, so the scale has to be applied
    // here. The month's peak day is the ceiling; without it every bar saturates.
    val peak = perDay.max().toFloat().coerceAtLeast(1f)

    return TrendUi(
      days = (1..daysInMonth).map { day ->
        TrendDayUi(
          value = perDay[day - 1].toFloat() / peak,
          today = isCurrentMonth && day == today.dayOfMonth,
          future = isCurrentMonth && day > today.dayOfMonth,
        )
      },
      title = UiText.Res(
        if (current.scope == ReportScope.EXPENSE) R.string.reports_trend_expense else R.string.reports_trend_income,
      ),
      paceLabel = UiText.Res(R.string.reports_pace, listOf(money.compact(Money(pace.toLong())))),
      paceFraction = pace / peak,
      todayAmount = if (isCurrentMonth) money.display(Money(perDay[today.dayOfMonth - 1])) else null,
    )
  }

  private fun Selection.kind() =
    if (scope == ReportScope.EXPENSE) TransactionKind.EXPENSE else TransactionKind.INCOME

  private data class Selection(val month: LocalDate, val scope: ReportScope)

  /** One month's raw reads, before they become UI. */
  private data class Month(
    val selection: Selection,
    val scoped: List<DatedAmount>,
    val totals: List<CategoryTotal>,
    val incomes: List<DatedAmount>,
    val expenses: List<DatedAmount>,
    val categories: List<Category>,
  )
}

private fun LocalDate.firstOfMonth() = LocalDate(year, monthNumber, 1)

private fun String.titleCase() = lowercase().replaceFirstChar { it.uppercase() }

private const val STOP_TIMEOUT_MS = 5_000L
