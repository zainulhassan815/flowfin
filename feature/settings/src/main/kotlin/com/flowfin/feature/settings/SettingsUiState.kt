package com.flowfin.feature.settings

import com.flowfin.core.model.ThemePreference

/**
 * Settings. Only the rows that do something today are here — the mockup's
 * currency, month-start, categories, and data rows each wait on work that
 * hasn't landed, and a row that opens nothing is worse than an absent one.
 */
data class SettingsUiState(
  val theme: ThemePreference = ThemePreference.LIGHT,
  val activeCategoryCount: Int = 0,
  val versionName: String = "",
  val versionCode: String = "",
)
