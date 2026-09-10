package com.flowfin.core.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * Which palette the app renders in. [LIGHT] is the default rather than [SYSTEM]:
 * FlowFin picks its own look unless the user says otherwise, and the light
 * palette is the one the app is designed against today.
 */
enum class ThemePreference { LIGHT, DARK, SYSTEM }

/**
 * Everything Settings persists. One serialized object rather than loose keys, so
 * adding a preference is a field with a default and old files keep parsing.
 */
@Serializable
data class UserSettings(
  val theme: ThemePreference = ThemePreference.LIGHT,
  val dailyReminderEnabled: Boolean = true,
  val dailyReminderTime: LocalTime = LocalTime(20, 0),
  val paymentAlertsEnabled: Boolean = true,
  val budgetAlertsEnabled: Boolean = true,
  /**
   * How far into a budget's monthly refill counts as "running low", as a whole
   * percent. Capped below 100 by the picker: [BudgetStatus.fraction] is clamped
   * to 1.0 so the progress bar can't run off the end, and a threshold above that
   * could never be crossed.
   */
  val budgetThreshold: Int = 80,
  /**
   * Which alerts have already been sent, so a bill that stays overdue and a
   * budget that stays over its threshold are announced once and not once a day.
   * Keyed by subject, valued by the occurrence it fired for — see `AlertLog`.
   *
   * Not a preference, and it rides here anyway: it is a handful of short strings
   * and a second DataStore to hold them would be more wiring than state.
   */
  val alertsFired: Map<String, String> = emptyMap(),
) {
  /** Whether anything needs scheduling at all. */
  val notificationsEnabled: Boolean
    get() = dailyReminderEnabled || paymentAlertsEnabled || budgetAlertsEnabled
}
