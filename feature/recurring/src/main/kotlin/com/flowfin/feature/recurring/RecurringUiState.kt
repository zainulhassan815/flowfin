package com.flowfin.feature.recurring

import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.ui.UiText

/**
 * The Recurring tab: active schedules split into **pending** (due now / overdue,
 * needing action) and **upcoming** (future firings, grouped by month), with a
 * counts line. Read-only for now — firing / skipping / adding arrive next.
 */
sealed interface RecurringUiState {
  data object Loading : RecurringUiState

  /** No schedules at all — an informational empty (no CTA until the Add flow lands). */
  data object Empty : RecurringUiState

  /** Schedules exist but every one is paused — distinct from [Empty] so the tab
   *  reads honestly. Resuming (and a paused list) arrive with the next slice. */
  data class AllPaused(val pausedCount: Int) : RecurringUiState

  data class Content(
    val pendingCount: Int,
    val activeCount: Int,
    val pending: List<RecurringPendingUi>,
    val upcoming: List<RecurringMonthGroup>,
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
