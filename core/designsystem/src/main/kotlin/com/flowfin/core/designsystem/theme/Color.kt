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
 *
 * Note the [accent] departs from `styles.css` and `design/system/brand.html`,
 * which are still on the brand's sand/cream `#E8DCC0`. The app's accent is now
 * deliberately near-neutral so that hue is free to mean something — see the
 * property's own note.
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
  /**
   * Deliberately near-neutral — near-black on light, near-white on dark. The
   * accent carries emphasis (buttons, the FAB, progress fills), not meaning:
   * colour in FlowFin is reserved for semantic state ([positive], [negative],
   * [warning], [transfer]) and category identity, so a hued accent would
   * compete with the only signals that are supposed to mean something.
   */
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

/**
 * The shipped dark palette. The surface ramp steps ~5 L* per level: at these
 * luminances a smaller step is invisible — the original ramp separated a card
 * from the page by 1.05:1, which read as one flat black field.
 */
val flowFinDarkColors = FlowFinColors(
  bg            = Color(0xFF08080A),
  surface       = Color(0xFF17171C),
  surface2      = Color(0xFF202027),
  surface3      = Color(0xFF2A2A32),
  border        = Color(0xFF34343E),
  borderStrong  = Color(0xFF46464F),
  pageBg        = Color(0xFF2A2A32),

  text          = Color(0xFFF2F2F4),
  textMute      = Color(0xFFB4B4BC),
  textSoft      = Color(0xFF9A9AA4),
  textFaint     = Color(0xFF7C7C88),

  accent        = Color(0xFFE8E8EA),
  positive      = Color(0xFF9CD4A2),
  warning       = Color(0xFFE8B66E),
  negative      = Color(0xFFE08A8A),
  transfer      = Color(0xFF82C5D4),
  onAccent      = Color(0xFF0A0A0B),

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
 * surfaces: the surface ramp steps *down* from a white page (each level a touch
 * darker, so cards read as tiles), and every semantic/category hue is darkened
 * to keep ≥4.5:1 against the surfaces it labels — the dark palette's pastels
 * wash out on white.
 *
 * The neutrals carry the same faint cool bias as the text ramp and as the dark
 * palette. They were originally cream, inherited from the sand brand accent,
 * which left warm cards sitting under cool grey type and kept a gold cast on
 * every surface after the accent itself went neutral.
 */
val flowFinLightColors = FlowFinColors(
  bg            = Color(0xFFFFFFFF),
  surface       = Color(0xFFFAFAFB),
  surface2      = Color(0xFFF3F3F6),
  surface3      = Color(0xFFEBEBEF),
  border        = Color(0xFFE3E3E8),
  borderStrong  = Color(0xFFD1D1D8),
  pageBg        = Color(0xFFEBEBEF),

  text          = Color(0xFF16161A),
  textMute      = Color(0xFF4A4A52),
  textSoft      = Color(0xFF6C6C76),
  textFaint     = Color(0xFF83838E),

  accent        = Color(0xFF22262B),
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
