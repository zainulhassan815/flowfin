package com.flowfin.feature.debts

import com.flowfin.core.ui.UiText

/** One-shot effects from [AddDebtViewModel], consumed in the entry's LaunchedEffect. */
sealed interface AddDebtEffect {
  data object Saved : AddDebtEffect
  data class ShowMessage(val text: UiText) : AddDebtEffect
}
