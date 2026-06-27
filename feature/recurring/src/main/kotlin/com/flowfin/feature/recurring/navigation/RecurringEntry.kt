package com.flowfin.feature.recurring.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.RecurringRoute
import com.flowfin.feature.recurring.RecurringScreen
import com.flowfin.feature.recurring.RecurringViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.recurringEntry() {
  entry<RecurringRoute> {
    val viewModel = koinViewModel<RecurringViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RecurringScreen(state = state)
  }
}
