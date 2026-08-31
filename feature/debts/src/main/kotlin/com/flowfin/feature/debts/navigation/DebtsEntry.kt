package com.flowfin.feature.debts.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AddDebtRoute
import com.flowfin.core.navigation.DebtDetailRoute
import com.flowfin.core.navigation.DebtsRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.feature.debts.DebtsScreen
import com.flowfin.feature.debts.DebtsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.debtsEntry(navigator: Navigator) {
  entry<DebtsRoute> {
    val viewModel = koinViewModel<DebtsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DebtsScreen(
      state = state,
      onDebtClick = { navigator.navigate(DebtDetailRoute(it.value.toString())) },
      onAddDebt = { navigator.navigate(AddDebtRoute) },
    )
  }
}
