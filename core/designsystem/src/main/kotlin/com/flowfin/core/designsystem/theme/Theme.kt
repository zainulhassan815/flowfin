package com.flowfin.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Wraps app content with FlowFin tokens and a Material 3 bridge so any M3
 * component used inside picks up the right palette and type without extra
 * configuration. Components inside should read tokens via [FlowFinTheme]:
 *
 * ```kotlin
 * Text(
 *   text = "Today",
 *   style = FlowFinTheme.typography.caption,
 *   color = FlowFinTheme.colors.textSoft,
 * )
 * ```
 *
 * Light is the default palette; pass [darkTheme] explicitly to pin the dark one
 * (previews, screenshots, a future in-app appearance setting). It does not follow
 * the system setting — FlowFin picks its own look until Settings offers a choice.
 */
@Composable
fun FlowFinTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colors = if (darkTheme) flowFinDarkColors else flowFinLightColors
  val typography = flowFinTypography

  CompositionLocalProvider(
    LocalFlowFinColors provides colors,
    LocalFlowFinTypography provides typography,
  ) {
    MaterialTheme(
      colorScheme = colors.toMaterialColorScheme(darkTheme),
      typography = typography.toMaterialTypography(),
      content = content,
    )
  }
}

/** Token accessors for use inside composables under [FlowFinTheme]. */
object FlowFinTheme {
  val colors: FlowFinColors
    @Composable @ReadOnlyComposable
    get() = LocalFlowFinColors.current

  val typography: FlowFinTypography
    @Composable @ReadOnlyComposable
    get() = LocalFlowFinTypography.current
}

// --- Material 3 bridge -------------------------------------------------------

/**
 * Maps FlowFin's palette onto M3's `ColorScheme` slots so stock M3 components
 * (Button, Card, TextField, …) inherit FlowFin colors when used as building
 * blocks. Custom FlowFin components should read [FlowFinTheme.colors] directly.
 */
private fun FlowFinColors.toMaterialColorScheme(darkTheme: Boolean): ColorScheme {
  val base = if (darkTheme) darkColorScheme() else lightColorScheme()
  return base.copy(
    background       = bg,
    surface          = surface,
    surfaceVariant   = surface2,
    surfaceContainer = surface3,
    outline          = border,
    outlineVariant   = borderStrong,

    onBackground       = text,
    onSurface          = text,
    onSurfaceVariant   = textMute,

    primary            = accent,
    onPrimary          = onAccent,
    secondary          = transfer,
    onSecondary        = onAccent,
    tertiary           = positive,
    onTertiary         = onAccent,
    error              = negative,
    onError            = onAccent,
  )
}

/**
 * Maps FlowFin's type scale onto M3's `Typography` slots. The fit is
 * approximate; FlowFin features use [FlowFinTheme.typography] for the
 * authoritative styles.
 */
private fun FlowFinTypography.toMaterialTypography() = Typography(
  displayLarge  = display,
  displayMedium = display,
  displaySmall  = h1,
  headlineLarge = h1,
  headlineMedium = h1,
  headlineSmall  = h2,
  titleLarge    = h2,
  titleMedium   = bodyLg,
  titleSmall    = body,
  bodyLarge     = bodyLg,
  bodyMedium    = body,
  bodySmall     = body,
  labelLarge    = label,
  labelMedium   = label,
  labelSmall    = caption,
)
