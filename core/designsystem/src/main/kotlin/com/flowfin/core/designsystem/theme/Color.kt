package com.flowfin.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * FlowFin's color tokens. One source of truth — every component reads from
 * [FlowFinTheme.colors] rather than hardcoding hex values.
 *
 * Mirrors the CSS custom properties in `design/system/styles.css`. The dark
 * palette is the default and currently the only one; a `lightColors` will
 * arrive when the design system covers light mode.
 */
@Immutable
data class FlowFinColors(
  // Surfaces
  val bg: Color,
  val surface: Color,
  val surface2: Color,
  val surface3: Color,
  val border: Color,
  val borderStrong: Color,
  val pageBg: Color,

  // Text
  val text: Color,
  val textMute: Color,
  val textSoft: Color,
  val textFaint: Color,

  // Semantic
  val accent: Color,
  val positive: Color,
  val warning: Color,
  val negative: Color,
  val transfer: Color,

  // Category palette — keyed by category role.
  val categories: CategoryColors,

  // Avatar tints for the person picker.
  val avatars: AvatarColors,
)

@Immutable
data class CategoryColors(
  val bank: Color,
  val cash: Color,
  val food: Color,
  val grocery: Color,
  val transport: Color,
  val fun_: Color,
  val salary: Color,
  val subs: Color,
  val rent: Color,
  val utilities: Color,
  val shop: Color,
  val health: Color,
  val edu: Color,
  val care: Color,
  val debt: Color,
  val other: Color,
)

@Immutable
data class AvatarColors(
  val tint1: Color,
  val tint2: Color,
  val tint3: Color,
  val tint4: Color,
  val tint5: Color,
) {
  /** Pick a tint by 1-based index. Out-of-range values fall back to [tint1]. */
  fun byIndex(index: Int): Color = when (index) {
    1 -> tint1
    2 -> tint2
    3 -> tint3
    4 -> tint4
    5 -> tint5
    else -> tint1
  }
}

/** The shipped dark palette. */
val flowFinDarkColors = FlowFinColors(
  bg            = Color(0xFF08080A),
  surface       = Color(0xFF101013),
  surface2      = Color(0xFF16161A),
  surface3      = Color(0xFF1C1C20),
  border        = Color(0xFF1E1E22),
  borderStrong  = Color(0xFF2A2A2F),
  pageBg        = Color(0xFF1C1C20),

  text          = Color(0xFFF2F2F4),
  textMute      = Color(0xFFB4B4BC),
  textSoft      = Color(0xFF8A8A92),
  textFaint     = Color(0xFF565660),

  accent        = Color(0xFFE8DCC0),
  positive      = Color(0xFF9CD4A2),
  warning       = Color(0xFFE8B66E),
  negative      = Color(0xFFE08A8A),
  transfer      = Color(0xFF82C5D4),

  categories = CategoryColors(
    bank      = Color(0xFF82C5D4),
    cash      = Color(0xFFC5D982),
    food      = Color(0xFFE8A87B),
    grocery   = Color(0xFFA8D479),
    transport = Color(0xFF8AB4E0),
    fun_      = Color(0xFFC98ED4),
    salary    = Color(0xFF9CD4A2),
    subs      = Color(0xFF9A8AE0),
    rent      = Color(0xFFD89A82),
    utilities = Color(0xFFE8CC7B),
    shop      = Color(0xFFE89AB8),
    health    = Color(0xFFDC8A8A),
    edu       = Color(0xFF8AC9D4),
    care      = Color(0xFFC9A8D4),
    debt      = Color(0xFFE08A8A),
    other     = Color(0xFFB4B4BC),
  ),

  avatars = AvatarColors(
    tint1 = Color(0xFF82C5D4),
    tint2 = Color(0xFFC98ED4),
    tint3 = Color(0xFFE8A87B),
    tint4 = Color(0xFF9CD4A2),
    tint5 = Color(0xFFE89AB8),
  ),
)

internal val LocalFlowFinColors = compositionLocalOf<FlowFinColors> {
  error("FlowFinColors not provided; wrap content in FlowFinTheme.")
}
