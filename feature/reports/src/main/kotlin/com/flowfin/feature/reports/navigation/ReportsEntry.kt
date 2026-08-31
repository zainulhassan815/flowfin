package com.flowfin.feature.reports.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.ReportsRoute
import com.flowfin.feature.reports.ReportsScreen
import com.flowfin.feature.reports.ReportsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.reportsEntry() {
  entry<ReportsRoute> {
    val viewModel = koinViewModel<ReportsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ReportsScreen(
      state = state,
      onPreviousMonth = viewModel::onPreviousMonth,
      onNextMonth = viewModel::onNextMonth,
      onSelectScope = viewModel::onSelectScope,
    )
  }
}
