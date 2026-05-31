package com.flowfin.feature.accounts

import com.flowfin.core.ui.AccountCardUi

/**
 * The Accounts tab. [Empty] is the whole-screen no-accounts treatment (the tab is
 * reachable from the nav bar even before anything exists); within [Content] an empty
 * [budgets] list just drops the Budget section to its "no budgets yet" hint.
 * There is no error state — a read failure is a crash, not a screen state.
 */
sealed interface AccountsListUiState {
  data object Loading : AccountsListUiState

  data object Empty : AccountsListUiState

  data class Content(
    val currency: String,
    val totalWhole: String,
    val totalDecimal: String,
    val realTotal: String,
    val budgetTotal: String,
    val accountCount: Int,
    val real: List<AccountCardUi>,
    val budgets: List<AccountCardUi>,
  ) : AccountsListUiState
}
