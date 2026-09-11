package com.flowfin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.model.ThemePreference
import com.flowfin.core.model.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalTime
import kotlinx.coroutines.launch

class SettingsViewModel(
  settings: SettingsRepository,
  categories: CategoryRepository,
  private val settingsRepository: SettingsRepository,
  private val appVersion: AppVersion,
) : ViewModel() {

  val uiState: StateFlow<SettingsUiState> = combine(
    settings.observe(),
    categories.observeAll(),
  ) { prefs, all ->
    SettingsUiState(
      theme = prefs.theme,
      appLockEnabled = prefs.appLockEnabled,
      dailyReminderEnabled = prefs.dailyReminderEnabled,
      dailyReminderTime = prefs.dailyReminderTime,
      paymentAlertsEnabled = prefs.paymentAlertsEnabled,
      budgetAlertsEnabled = prefs.budgetAlertsEnabled,
      budgetThreshold = prefs.budgetThreshold,
      activeCategoryCount = all.count { !it.isArchived },
      versionName = appVersion.name,
      versionCode = appVersion.code,
    )
  }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
      SettingsUiState(versionName = appVersion.name, versionCode = appVersion.code),
    )

  fun onThemeChange(theme: ThemePreference) = edit { it.copy(theme = theme) }

  fun onAppLockChange(enabled: Boolean) = edit { it.copy(appLockEnabled = enabled) }

  fun onDailyReminderChange(enabled: Boolean) = edit { it.copy(dailyReminderEnabled = enabled) }

  fun onReminderTimeChange(time: LocalTime) = edit { it.copy(dailyReminderTime = time) }

  fun onPaymentAlertsChange(enabled: Boolean) = edit { it.copy(paymentAlertsEnabled = enabled) }

  fun onBudgetAlertsChange(enabled: Boolean) = edit { it.copy(budgetAlertsEnabled = enabled) }

  fun onBudgetThresholdChange(percent: Int) = edit { it.copy(budgetThreshold = percent) }

  private fun edit(transform: (UserSettings) -> UserSettings) {
    viewModelScope.launch { settingsRepository.update(transform) }
  }
}

/** The running build's version, read once by the app and handed down. */
data class AppVersion(val name: String, val code: String)

private const val STOP_TIMEOUT_MS = 5_000L
