package com.flowfin.feature.home.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AccountsRoute
import com.flowfin.core.navigation.AddAccountRoute
import com.flowfin.core.navigation.HomeRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.RecurringRoute
import com.flowfin.core.navigation.TransactionDetailRoute
import com.flowfin.core.navigation.TransactionsRoute
import com.flowfin.feature.home.HomeScreen
import com.flowfin.feature.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.homeEntry(navigator: Navigator) {
  entry<HomeRoute> {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
      state = state,
      onAllAccounts = { navigator.navigate(AccountsRoute) },
      onAllPending = { navigator.navigate(RecurringRoute) },
      onAllRecent = { navigator.navigate(TransactionsRoute) },
      onAddAccount = { navigator.navigate(AddAccountRoute) },
      onTransactionClick = { id -> navigator.navigate(TransactionDetailRoute(id.value.toString())) },
      // TODO: wire once their destinations exist — onAccountClick (detail screen),
      //  onPayPending (fire-schedule flow), onSearch, onSettings. They no-op until then.
    )
  }
}
