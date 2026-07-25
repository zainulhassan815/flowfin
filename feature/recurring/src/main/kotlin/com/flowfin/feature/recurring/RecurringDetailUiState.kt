package com.flowfin.feature.recurring

import com.flowfin.core.ui.UiText

/**
 * Recurring schedule detail: the hero (amount, category), a schedule/next-due
 * fact rail, the money-path node (account), and Pause/Resume + Delete actions.
 * No edit yet — [AddRecurringViewModel] is create-only; editing an existing
 * schedule is tracked separately.
 */
sealed interface RecurringDetailUiState {
  data object Loading : RecurringDetailUiState

  /** The schedule was deleted (or never existed) — the screen shows an unavailable note. */
  data object NotFound : RecurringDetailUiState

  data class Content(
    val name: String,
    val kindTitle: UiText,
    val currency: String,
    val amountWhole: String,
    val amountDecimal: String,
    val heroIconKey: String?,
    val heroColorKey: String?,
    val isActive: Boolean,
    val status: UiText,
    val scheduleLabel: UiText,
    val nextDue: UiText?,
    val account: RecurringDetailNode?,
    val category: RecurringDetailNode?,
  ) : RecurringDetailUiState
}

/** One node of the detail's money-path rail — an account or a category. */
data class RecurringDetailNode(
  val role: UiText,
  val name: UiText,
  val iconKey: String?,
  val colorKey: String?,
)
