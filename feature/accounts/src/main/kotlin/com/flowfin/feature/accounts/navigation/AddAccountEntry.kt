package com.flowfin.feature.accounts.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AddAccountRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.ui.resolve
import com.flowfin.feature.accounts.AddAccountEffect
import com.flowfin.feature.accounts.AddAccountScreen
import com.flowfin.feature.accounts.AddAccountViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.addAccountEntry(navigator: Navigator) {
  entry<AddAccountRoute> {
    val viewModel = koinViewModel<AddAccountViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          AddAccountEffect.NavigateBack -> navigator.goBack()
          is AddAccountEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    AddAccountScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onNameChange = viewModel::onNameChange,
      onSelectKind = viewModel::onSelectKind,
      onBalanceChange = viewModel::onBalanceChange,
      onSave = viewModel::save,
    )
  }
}
