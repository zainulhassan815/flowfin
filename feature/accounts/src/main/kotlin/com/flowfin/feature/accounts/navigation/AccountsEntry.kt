package com.flowfin.feature.accounts.navigation

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
import com.flowfin.core.navigation.AddBudgetRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.ui.resolve
import com.flowfin.feature.accounts.AccountsListScreen
import com.flowfin.feature.accounts.AccountsListViewModel
import com.flowfin.feature.accounts.AddBudgetEffect
import com.flowfin.feature.accounts.AddBudgetScreen
import com.flowfin.feature.accounts.AddBudgetViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.accountsEntry(navigator: Navigator) {
  entry<AccountsRoute> {
    val viewModel = koinViewModel<AccountsListViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AccountsListScreen(
      state = state,
      onAddAccount = { navigator.navigate(AddAccountRoute) },
      onAccountClick = { id -> navigator.navigate(AccountDetailRoute(id.value.toString())) },
      onSetBudget = { navigator.navigate(AddBudgetRoute) },
    )
  }

  entry<AddBudgetRoute> {
    val viewModel = koinViewModel<AddBudgetViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          AddBudgetEffect.NavigateBack -> navigator.goBack()
          is AddBudgetEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    AddBudgetScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = navigator::goBack,
      onNameChange = viewModel::onNameChange,
      onSelectIcon = viewModel::onSelectIcon,
      onSelectColor = viewModel::onSelectColor,
      onSelectParent = viewModel::onSelectParent,
      onToggleRefill = viewModel::onToggleRefill,
      onSelectRefillDay = viewModel::onSelectRefillDay,
      onToggleFundNow = viewModel::onToggleFundNow,
      onOpenSheet = viewModel::onOpenSheet,
      onDismissSheet = viewModel::onDismissSheet,
      onFocusAmount = viewModel::onFocusAmount,
      onBlurAmount = viewModel::onBlurAmount,
      onKey = viewModel::onKey,
      onSave = viewModel::save,
    )
  }
}
