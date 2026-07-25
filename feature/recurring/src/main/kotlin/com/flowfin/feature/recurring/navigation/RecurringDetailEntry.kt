package com.flowfin.feature.recurring.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.RecurringDetailRoute
import com.flowfin.core.ui.resolve
import com.flowfin.feature.recurring.RecurringDetailEffect
import com.flowfin.feature.recurring.RecurringDetailScreen
import com.flowfin.feature.recurring.RecurringDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

fun EntryProviderScope<NavKey>.recurringDetailEntry(navigator: Navigator) {
  entry<RecurringDetailRoute> { route ->
    val scheduleId = RecurringScheduleId(Uuid.parse(route.scheduleId))
    val viewModel = koinViewModel<RecurringDetailViewModel> { parametersOf(scheduleId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          RecurringDetailEffect.Dismiss -> navigator.goBack()
          is RecurringDetailEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    RecurringDetailScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onTogglePause = viewModel::togglePause,
      onConfirmDelete = viewModel::delete,
    )
  }
}
