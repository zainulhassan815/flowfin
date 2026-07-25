package com.flowfin.feature.recurring

import com.flowfin.core.ui.UiText

/** One-shot effects from [RecurringDetailViewModel], consumed in the entry's LaunchedEffect. */
sealed interface RecurringDetailEffect {
  data object Dismiss : RecurringDetailEffect
  data class ShowMessage(val text: UiText) : RecurringDetailEffect
}
