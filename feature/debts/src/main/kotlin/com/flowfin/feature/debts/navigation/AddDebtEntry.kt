package com.flowfin.feature.debts.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AddDebtRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.ui.resolve
import com.flowfin.feature.debts.AddDebtEffect
import com.flowfin.feature.debts.AddDebtScreen
import com.flowfin.feature.debts.AddDebtViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.addDebtEntry(navigator: Navigator) {
  entry<AddDebtRoute> {
    val viewModel = koinViewModel<AddDebtViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          AddDebtEffect.Saved -> navigator.goBack()
          is AddDebtEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    AddDebtScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onSelectDirection = viewModel::onSelectDirection,
      onKey = viewModel::onKey,
      onOpenSheet = viewModel::onOpenSheet,
      onDismissSheet = viewModel::onDismissSheet,
      onPersonQueryChange = viewModel::onPersonQueryChange,
      onPickPerson = viewModel::onPickPerson,
      onUseTypedPerson = viewModel::onUseTypedPerson,
      onReasonChange = viewModel::onReasonChange,
      onLinkAccountChange = viewModel::onLinkAccountChange,
      onPickAccount = viewModel::onPickAccount,
      onSave = viewModel::save,
    )
  }
}
