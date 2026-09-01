package com.flowfin.feature.accounts

import com.flowfin.core.ui.UiText

/** One-shot outcomes of the Add-Budget form. */
sealed interface AddBudgetEffect {
  data object NavigateBack : AddBudgetEffect
  data class ShowMessage(val text: UiText) : AddBudgetEffect
}
