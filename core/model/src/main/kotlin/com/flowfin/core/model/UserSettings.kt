package com.flowfin.core.model

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
)
