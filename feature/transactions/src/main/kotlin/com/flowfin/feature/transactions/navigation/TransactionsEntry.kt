package com.flowfin.feature.transactions.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AddTransactionRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.TransactionsRoute
import com.flowfin.feature.transactions.TransactionsListScreen
import com.flowfin.feature.transactions.TransactionsListViewModel
import org.koin.compose.viewmodel.koinViewModel

/** The full transactions ledger — reached from Home's "Recent · All / History". */
fun EntryProviderScope<NavKey>.transactionsEntry(navigator: Navigator) {
  entry<TransactionsRoute> {
    val viewModel = koinViewModel<TransactionsListViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionsListScreen(
      state = state,
      onBack = { navigator.goBack() },
      onFilterChange = viewModel::setFilter,
      onAddTransaction = { navigator.navigate(AddTransactionRoute) },
      // onTransactionClick is wired once Transaction detail (FLO-23) exists.
    )
  }
}
