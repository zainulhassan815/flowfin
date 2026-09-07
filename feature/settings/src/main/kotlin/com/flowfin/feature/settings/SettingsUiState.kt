package com.flowfin.feature.settings

import com.flowfin.core.model.ThemePreference
import kotlinx.datetime.LocalTime

/**
 * Settings. Only the rows that do something today are here — the mockup's
 * currency, month-start, and data rows each wait on work that hasn't landed,
 * and a row that opens nothing is worse than an absent one.
 */
data class SettingsUiState(
  val theme: ThemePreference = ThemePreference.LIGHT,
  val dailyReminderEnabled: Boolean = true,
  val dailyReminderTime: LocalTime = LocalTime(20, 0),
  val paymentAlertsEnabled: Boolean = true,
  val budgetAlertsEnabled: Boolean = true,
  val activeCategoryCount: Int = 0,
  val versionName: String = "",
  val versionCode: String = "",
)
