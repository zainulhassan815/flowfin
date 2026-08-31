package com.flowfin.feature.settings.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.CategoriesRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.ui.resolve
import com.flowfin.feature.settings.CategoriesEffect
import com.flowfin.feature.settings.CategoriesScreen
import com.flowfin.feature.settings.CategoriesViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.categoriesEntry(navigator: Navigator) {
  entry<CategoriesRoute> {
    val viewModel = koinViewModel<CategoriesViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
      viewModel.effects.collect { effect ->
        when (effect) {
          is CategoriesEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(context))
        }
      }
    }

    CategoriesScreen(
      state = state,
      snackbarHostState = snackbarHostState,
      onBack = { navigator.goBack() },
      onSelectScope = viewModel::onSelectScope,
      onAdd = viewModel::onAdd,
      onEdit = viewModel::onEdit,
      onToggleArchived = viewModel::onToggleArchived,
      onDismissSheet = viewModel::onDismissSheet,
      onNameChange = viewModel::onNameChange,
      onIconChange = viewModel::onIconChange,
      onColorChange = viewModel::onColorChange,
      onArchive = viewModel::onArchive,
      onUnarchive = viewModel::onUnarchive,
      onSave = viewModel::onSave,
    )
  }
}
