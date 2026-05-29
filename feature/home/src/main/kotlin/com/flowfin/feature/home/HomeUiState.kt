package com.flowfin.feature.home

import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.TxRowUi

/**
 * Home's states, derived in the ViewModel. [Empty] is the F full-empty treatment
 * (no accounts yet); within [Content], an empty [recent] is the S section hint.
 * There is no error state — a read failure is catastrophic, not a Home state.
 */
sealed interface HomeUiState {
  data object Loading : HomeUiState

  data object Empty : HomeUiState

  data class Content(
    val totalWhole: String,
    val totalDecimal: String,
    val allocated: String,
    val accounts: List<AccountCardUi>,
    val recent: List<TxRowUi>,
  ) : HomeUiState
}
