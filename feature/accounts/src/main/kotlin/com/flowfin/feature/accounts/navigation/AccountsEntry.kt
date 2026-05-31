package com.flowfin.feature.accounts.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AccountDetailRoute
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
      onAccountClick = { id -> navigator.navigate(AccountDetailRoute(id.value.toString())) },
      // TODO: onSetBudget (Add-Budget flow) no-ops until that screen exists.
    )
  }
}
