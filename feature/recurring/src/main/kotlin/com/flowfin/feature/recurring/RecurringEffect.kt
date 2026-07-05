package com.flowfin.feature.recurring

import com.flowfin.core.ui.UiText

/** One-shot effects from the Recurring ViewModel, consumed in the entry's LaunchedEffect. */
sealed interface RecurringEffect {
  data class ShowMessage(val text: UiText) : RecurringEffect
}
