package com.flowfin.feature.recurring.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.RecurringRoute
import com.flowfin.core.ui.resolve
import com.flowfin.feature.recurring.RecurringEffect
import com.flowfin.feature.recurring.RecurringScreen
import com.flowfin.feature.recurring.RecurringViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.recurringEntry() {
  entry<RecurringRoute> {
    val viewModel = koinViewModel<RecurringViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          is RecurringEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    RecurringScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onMarkPaid = viewModel::markPaid,
      onSkip = viewModel::skip,
    )
  }
}
