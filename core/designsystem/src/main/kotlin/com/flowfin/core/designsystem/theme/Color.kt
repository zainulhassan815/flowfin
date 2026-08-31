package com.flowfin.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * FlowFin's color tokens. One source of truth — every component reads from
 * [FlowFinTheme.colors] rather than hardcoding hex values.
 *
 * Mirrors the CSS custom properties in `design/system/styles.css`. Two palettes
 * ship: [flowFinDarkColors] (the reference palette the design system was drawn
 * in) and [flowFinLightColors] (the same structure re-tuned for light surfaces).
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

  /** Content color for text/icons sitting on an [accent] or [negative] fill. */
  val onAccent: Color,

  // Category palette — keyed by category role.
  val categories: CategoryColors,

  // Avatar tints for the person picker.
  val avatars: AvatarColors,
)

@Immutable
data class CategoryColors(
  val bank: Color,
  val cash: Color,
  val mobile: Color,
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
  onAccent      = Color(0xFF08080A),

  categories = CategoryColors(
    bank      = Color(0xFF82C5D4),
    cash      = Color(0xFFC5D982),
    mobile    = Color(0xFFC98ED4),
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

/**
 * The light palette. Same structure as [flowFinDarkColors], re-tuned for light
 * surfaces: the surface ramp steps *away* from a white page (each level a touch
 * warmer/darker, so cards read as tiles), and every semantic/category hue is
 * darkened to keep ≥4.5:1 against the surfaces it labels — the dark palette's
 * pastels wash out on white.
 */
val flowFinLightColors = FlowFinColors(
  bg            = Color(0xFFFFFFFF),
  surface       = Color(0xFFF8F7F4),
  surface2      = Color(0xFFF1F0EB),
  surface3      = Color(0xFFE9E8E2),
  border        = Color(0xFFE5E4DE),
  borderStrong  = Color(0xFFD4D3CC),
  pageBg        = Color(0xFFE9E8E2),

  text          = Color(0xFF16161A),
  textMute      = Color(0xFF4A4A52),
  textSoft      = Color(0xFF74747E),
  textFaint     = Color(0xFF9E9EA8),

  accent        = Color(0xFF6E5D33),
  positive      = Color(0xFF2E7D48),
  warning       = Color(0xFF9A6A17),
  negative      = Color(0xFFB3413C),
  transfer      = Color(0xFF2A7A8C),
  onAccent      = Color(0xFFFFFFFF),

  categories = CategoryColors(
    bank      = Color(0xFF2F7C8E),
    cash      = Color(0xFF6B8A2E),
    mobile    = Color(0xFF8B4E97),
    food      = Color(0xFFB4653A),
    grocery   = Color(0xFF5C8B32),
    transport = Color(0xFF3D6FA5),
    fun_      = Color(0xFF8B4E97),
    salary    = Color(0xFF2E7D48),
    subs      = Color(0xFF5B4CB0),
    rent      = Color(0xFFA0563C),
    utilities = Color(0xFF8A6F1E),
    shop      = Color(0xFFB0466C),
    health    = Color(0xFFA8443F),
    edu       = Color(0xFF2C7B87),
    care      = Color(0xFF7E5A8E),
    debt      = Color(0xFFB3413C),
    other     = Color(0xFF6C6C76),
  ),

  avatars = AvatarColors(
    tint1 = Color(0xFF2F7C8E),
    tint2 = Color(0xFF8B4E97),
    tint3 = Color(0xFFB4653A),
    tint4 = Color(0xFF2E7D48),
    tint5 = Color(0xFFB0466C),
  ),
)

internal val LocalFlowFinColors = compositionLocalOf<FlowFinColors> {
  error("FlowFinColors not provided; wrap content in FlowFinTheme.")
}
