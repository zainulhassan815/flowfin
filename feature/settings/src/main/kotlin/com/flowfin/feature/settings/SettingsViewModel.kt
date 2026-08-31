package com.flowfin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.model.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val settings: SettingsRepository,
  private val appVersion: AppVersion,
) : ViewModel() {

  val uiState: StateFlow<SettingsUiState> = settings.observe()
    .map { SettingsUiState(theme = it.theme, versionName = appVersion.name, versionCode = appVersion.code) }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
      SettingsUiState(versionName = appVersion.name, versionCode = appVersion.code),
    )

  fun onThemeChange(theme: ThemePreference) {
    viewModelScope.launch { settings.setTheme(theme) }
  }
}

/** The running build's version, read once by the app and handed down. */
data class AppVersion(val name: String, val code: String)

private const val STOP_TIMEOUT_MS = 5_000L
