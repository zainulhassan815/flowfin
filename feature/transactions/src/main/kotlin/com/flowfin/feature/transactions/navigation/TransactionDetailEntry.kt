package com.flowfin.feature.transactions.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.model.TransactionId
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.TransactionDetailRoute
import com.flowfin.core.ui.resolve
import com.flowfin.feature.transactions.TransactionDetailEffect
import com.flowfin.feature.transactions.TransactionDetailScreen
import com.flowfin.feature.transactions.TransactionDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

fun EntryProviderScope<NavKey>.transactionDetailEntry(navigator: Navigator) {
  entry<TransactionDetailRoute> { route ->
    val transactionId = TransactionId(Uuid.parse(route.transactionId))
    val viewModel = koinViewModel<TransactionDetailViewModel> { parametersOf(transactionId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          TransactionDetailEffect.Dismiss -> navigator.goBack()
          is TransactionDetailEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    TransactionDetailScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onConfirmDelete = viewModel::delete,
    )
  }
}
