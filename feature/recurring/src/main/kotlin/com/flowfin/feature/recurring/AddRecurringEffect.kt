package com.flowfin.feature.recurring

import com.flowfin.core.ui.UiText

/** One-shot effects from the Add-Recurring ViewModel, consumed in the entry's LaunchedEffect. */
sealed interface AddRecurringEffect {
  data object NavigateBack : AddRecurringEffect
  data class ShowMessage(val text: UiText) : AddRecurringEffect
}
