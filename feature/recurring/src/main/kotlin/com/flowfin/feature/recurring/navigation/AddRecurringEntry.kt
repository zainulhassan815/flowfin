package com.flowfin.feature.recurring.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AddRecurringRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.ui.resolve
import com.flowfin.feature.recurring.AddRecurringEffect
import com.flowfin.feature.recurring.AddRecurringScreen
import com.flowfin.feature.recurring.AddRecurringViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.addRecurringEntry(navigator: Navigator) {
  entry<AddRecurringRoute> {
    val viewModel = koinViewModel<AddRecurringViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          AddRecurringEffect.NavigateBack -> navigator.goBack()
          is AddRecurringEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    AddRecurringScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onSelectType = viewModel::onSelectType,
      onKey = viewModel::onKey,
      onNameChange = viewModel::onNameChange,
      onSelectFrequency = viewModel::onSelectFrequency,
      onOpenSheet = viewModel::openSheet,
      onFocusAmount = viewModel::onFocusAmount,
      onBlurAmount = viewModel::onBlurAmount,
      onDismissSheet = viewModel::dismissSheet,
      onPickWeekday = viewModel::onPickWeekday,
      onPickMonthDay = viewModel::onPickMonthDay,
      onPickYearlyMonth = viewModel::onPickYearlyMonth,
      onPickYearlyDay = viewModel::onPickYearlyDay,
      onPickAccount = viewModel::onPickAccount,
      onPickCategory = viewModel::onPickCategory,
      onNoteChange = viewModel::onNoteChange,
      onSave = viewModel::save,
    )
  }
}
