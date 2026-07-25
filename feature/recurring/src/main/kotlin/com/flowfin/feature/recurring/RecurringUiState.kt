package com.flowfin.feature.recurring

import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.ui.UiText

/**
 * The Recurring tab: one continuous flow of sections — **pending** (due now /
 * overdue, needing action), **active** (future firings, grouped by month), and
 * **paused** — the same section-per-slice shape every other tab uses, so a
 * schedule never lives behind a separate screen just because it's paused.
 */
sealed interface RecurringUiState {
  data object Loading : RecurringUiState

  /** No schedules at all — an informational empty (no CTA until the Add flow lands). */
  data object Empty : RecurringUiState

  data class Content(
    val pendingCount: Int,
    val activeCount: Int,
    val monthlyTotalWhole: String,
    val monthlyTotalDecimal: String,
    val pending: List<RecurringPendingUi>,
    val upcoming: List<RecurringMonthGroup>,
    val paused: List<RecurringPausedUi>,
  ) : RecurringUiState
}

/** [Due] maps to the warning tint, [Late] to the negative tint. */
enum class RecurringUrgency { Due, Late }

/** A schedule needing action — due today or overdue. */
data class RecurringPendingUi(
  val id: RecurringScheduleId,
  val name: String,
  val schedule: UiText,
  val status: UiText,
  val urgency: RecurringUrgency,
  val amountWhole: String,
  val amountDecimal: String,
  val iconKey: String?,
  val colorKey: String?,
)

/** A future firing in the upcoming list. */
data class RecurringUpcomingUi(
  val id: RecurringScheduleId,
  val name: String,
  val freq: UiText,
  val due: UiText,
  val amountWhole: String,
  val amountDecimal: String,
  val iconKey: String?,
  val colorKey: String?,
)

/** Upcoming rows under a month heading, e.g. "June 2026". */
data class RecurringMonthGroup(
  val label: UiText,
  val rows: List<RecurringUpcomingUi>,
)

/** A paused schedule — the tab's Paused section. */
data class RecurringPausedUi(
  val id: RecurringScheduleId,
  val name: String,
  val freq: UiText,
  val pausedSince: UiText,
  val amountWhole: String,
  val amountDecimal: String,
  val iconKey: String?,
  val colorKey: String?,
)
