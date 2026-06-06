package com.flowfin.feature.transactions.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowfin.core.ui.resolve
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AddTransactionRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.feature.transactions.AddTransactionEffect
import com.flowfin.feature.transactions.AddTransactionScreen
import com.flowfin.feature.transactions.AddTransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.addTransactionEntry(navigator: Navigator) {
  entry<AddTransactionRoute> {
    val viewModel = koinViewModel<AddTransactionViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          AddTransactionEffect.NavigateBack -> navigator.goBack()
          is AddTransactionEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    AddTransactionScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onSelectType = viewModel::onSelectType,
      onKey = viewModel::onKey,
      onOpenSheet = viewModel::openSheet,
      onDismissSheet = viewModel::dismissSheet,
      onPickFromAccount = viewModel::onPickFromAccount,
      onPickToAccount = viewModel::onPickToAccount,
      onPickCategory = viewModel::onPickCategory,
      onPickDate = viewModel::onPickDate,
      onNoteChange = viewModel::onNoteChange,
      onSave = viewModel::save,
    )
  }
}
