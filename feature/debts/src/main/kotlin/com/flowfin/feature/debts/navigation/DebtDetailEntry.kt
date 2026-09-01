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
import com.flowfin.core.navigation.DebtDetailRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.RecordPaymentRoute
import com.flowfin.core.ui.resolve
import com.flowfin.feature.debts.DebtDetailEffect
import com.flowfin.feature.debts.DebtDetailScreen
import com.flowfin.feature.debts.DebtDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

fun EntryProviderScope<NavKey>.debtDetailEntry(navigator: Navigator) {
  entry<DebtDetailRoute> { route ->
    val debtId = DebtId(Uuid.parse(route.debtId))
    val viewModel = koinViewModel<DebtDetailViewModel> { parametersOf(debtId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          DebtDetailEffect.Dismiss -> navigator.goBack()
          is DebtDetailEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    DebtDetailScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onRecordPayment = { navigator.navigate(RecordPaymentRoute(route.debtId)) },
      onToggleSettled = viewModel::toggleSettled,
      onConfirmDelete = viewModel::delete,
    )
  }
}
