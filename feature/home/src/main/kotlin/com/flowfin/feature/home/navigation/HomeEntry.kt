package com.flowfin.feature.home.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AccountDetailRoute
import com.flowfin.core.navigation.AccountsRoute
import com.flowfin.core.navigation.AddAccountRoute
import com.flowfin.core.navigation.HomeRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.RecurringRoute
import com.flowfin.core.navigation.SettingsRoute
import com.flowfin.core.navigation.TransactionDetailRoute
import com.flowfin.core.navigation.TransactionsRoute
import com.flowfin.core.ui.resolve
import com.flowfin.feature.home.HomeEffect
import com.flowfin.feature.home.HomeScreen
import com.flowfin.feature.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.homeEntry(navigator: Navigator) {
  entry<HomeRoute> {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          is HomeEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    HomeScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onAllAccounts = { navigator.navigate(AccountsRoute) },
      onAllPending = { navigator.navigate(RecurringRoute) },
      onAllRecent = { navigator.navigate(TransactionsRoute) },
      onAddAccount = { navigator.navigate(AddAccountRoute) },
      onAccountClick = { id -> navigator.navigate(AccountDetailRoute(id.value.toString())) },
      onTransactionClick = { id -> navigator.navigate(TransactionDetailRoute(id.value.toString())) },
      onPayPending = viewModel::payPending,
      onSettings = { navigator.navigate(SettingsRoute) },
    )
  }
}
