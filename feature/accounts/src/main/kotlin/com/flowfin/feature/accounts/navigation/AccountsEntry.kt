package com.flowfin.feature.accounts.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AccountsRoute
import com.flowfin.core.navigation.AddAccountRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.feature.accounts.AccountsListScreen
import com.flowfin.feature.accounts.AccountsListViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.accountsEntry(navigator: Navigator) {
  entry<AccountsRoute> {
    val viewModel = koinViewModel<AccountsListViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AccountsListScreen(
      state = state,
      onAddAccount = { navigator.navigate(AddAccountRoute) },
      // TODO: no destinations yet — onAccountClick (account detail, FLO-15) and
      //  onSetBudget (Add-Budget flow) no-op until those screens exist.
    )
  }
}
