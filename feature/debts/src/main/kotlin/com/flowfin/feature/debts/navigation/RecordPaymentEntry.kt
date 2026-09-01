package com.flowfin.feature.debts.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.model.DebtId
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.RecordPaymentRoute
import com.flowfin.core.ui.resolve
import com.flowfin.feature.debts.RecordPaymentEffect
import com.flowfin.feature.debts.RecordPaymentScreen
import com.flowfin.feature.debts.RecordPaymentViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

fun EntryProviderScope<NavKey>.recordPaymentEntry(navigator: Navigator) {
  entry<RecordPaymentRoute> { route ->
    val debtId = DebtId(Uuid.parse(route.debtId))
    val viewModel = koinViewModel<RecordPaymentViewModel> { parametersOf(debtId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          RecordPaymentEffect.NavigateBack -> navigator.goBack()
          is RecordPaymentEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    RecordPaymentScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onKey = viewModel::onKey,
      onFocusAmount = viewModel::onFocusAmount,
      onBlurAmount = viewModel::onBlurAmount,
      onLinkAccountChange = viewModel::onLinkAccountChange,
      onAccountSelected = viewModel::onAccountSelected,
      onNoteChange = viewModel::onNoteChange,
      onOpenSheet = viewModel::onOpenSheet,
      onDismissSheet = viewModel::onDismissSheet,
      onPickDate = viewModel::onPickDate,
      onSave = viewModel::save,
    )
  }
}
