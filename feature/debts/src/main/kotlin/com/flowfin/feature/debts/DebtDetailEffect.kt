package com.flowfin.feature.debts

import com.flowfin.core.ui.UiText

/** One-shot effects from [DebtDetailViewModel], consumed in the entry's LaunchedEffect. */
sealed interface DebtDetailEffect {
  data object Dismiss : DebtDetailEffect
  data class ShowMessage(val text: UiText) : DebtDetailEffect
}
