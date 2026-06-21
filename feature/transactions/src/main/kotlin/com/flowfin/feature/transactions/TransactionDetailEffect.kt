package com.flowfin.feature.transactions

import com.flowfin.core.ui.UiText

/** One-shot effects from the detail ViewModel, consumed in the entry's LaunchedEffect. */
sealed interface TransactionDetailEffect {
  /** The row was deleted — leave the screen. */
  data object Dismiss : TransactionDetailEffect
  data class ShowMessage(val text: UiText) : TransactionDetailEffect
}
