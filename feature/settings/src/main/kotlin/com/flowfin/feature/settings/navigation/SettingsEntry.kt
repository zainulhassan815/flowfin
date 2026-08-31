package com.flowfin.feature.settings.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.CategoriesRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.SettingsRoute
import com.flowfin.feature.settings.SettingsScreen
import com.flowfin.feature.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.settingsEntry(navigator: Navigator) {
  entry<SettingsRoute> {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
      state = state,
      onBack = { navigator.goBack() },
      onThemeChange = viewModel::onThemeChange,
      onCategories = { navigator.navigate(CategoriesRoute) },
    )
  }
}
