package com.flowfin.feature.reports

import com.flowfin.core.ui.UiText

/** Which side of the ledger the trend and breakdown describe. */
enum class ReportScope { EXPENSE, INCOME }

sealed interface ReportsUiState {
  data object Loading : ReportsUiState

  /** No transactions have ever been recorded — a period picker would be theatre. */
  data object Empty : ReportsUiState

  data class Content(
    val monthLabel: String,
    val yearLabel: String,
    /** False on the current month: there is nothing ahead to look at. */
    val canGoForward: Boolean,
    val scope: ReportScope,
    val netWhole: String,
    val netDecimal: String,
    val netIsPositive: Boolean,
    val incomeTotal: String,
    val expenseTotal: String,
    /** Null when the selected month has no rows in the current scope. */
    val trend: TrendUi?,
    val breakdown: List<BreakdownRowUi>,
    /** Compact and unsymbolled: the donut's hole is narrower than a full form. */
    val breakdownTotal: String,
    val breakdownCount: Int,
  ) : ReportsUiState
}

/**
 * One bar per day of the selected month. Days after today in the current month
 * are [TrendDayUi.future] so the axis keeps its width without implying zero
 * spend on days that haven't happened.
 */
data class TrendUi(
  val days: List<TrendDayUi>,
  val title: UiText,
  val paceLabel: UiText,
  /** Pace as a share of the month's peak day — FlowFinTrendChart wants 0..1. */
  val paceFraction: Float,
  val todayAmount: String?,
)

/** [value] is a share of the month's peak day, which is the scale the chart draws against. */
data class TrendDayUi(val value: Float, val today: Boolean, val future: Boolean)

data class BreakdownRowUi(
  val name: String,
  /** A `FlowFinColors.categories` key; the screen resolves it via categoryColor. */
  val colorKey: String?,
  val amountWhole: String,
  val amountDecimal: String,
  /** Share of the period's total, whole percent. */
  val percent: Int,
  val transactionCount: Int,
  /** Raw minor units — the donut sizes arcs off this, not the rounded percent. */
  val value: Float,
)
