@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.flowfin.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.R

/**
 * Two typefaces only: Literata for editorial prose (body, headings) and Geist
 * Mono for numbers, labels, and any structural / technical-feeling text.
 *
 * Both ship as variable fonts; weight is selected per [Font] entry via
 * [FontVariation.Settings].
 */

private fun literataFont(weight: Int) = Font(
  resId = R.font.literata,
  weight = FontWeight(weight),
  style = FontStyle.Normal,
  variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun literataItalicFont(weight: Int) = Font(
  resId = R.font.literata_italic,
  weight = FontWeight(weight),
  style = FontStyle.Italic,
  variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun geistMonoFont(weight: Int) = Font(
  resId = R.font.geist_mono,
  weight = FontWeight(weight),
  style = FontStyle.Normal,
  variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Literata: FontFamily = FontFamily(
  literataFont(300),
  literataFont(400),
  literataFont(500),
  literataItalicFont(300),
  literataItalicFont(400),
  literataItalicFont(500),
)

val GeistMono: FontFamily = FontFamily(
  geistMonoFont(300),
  geistMonoFont(400),
  geistMonoFont(500),
  geistMonoFont(600),
)

/**
 * FlowFin's type scale. Each style is a finished [TextStyle] — components
 * read whole styles, not individual properties.
 *
 * Mirrors `design/system/foundations.html` exactly. Letter-spacing values
 * use `.em` so they scale with font size automatically.
 */
@Immutable
data class FlowFinTypography(
  /** Geist Mono · 56 / 300 · -0.04em. The headline number on Home. */
  val hero: TextStyle,
  /** Literata Italic · 48 / 400 · -0.03em. Reserved for marketing / onboarding moments. */
  val display: TextStyle,
  /** Literata Italic · 28 / 400 · -0.02em. Page titles. */
  val h1: TextStyle,
  /** Literata Italic · 22 / 400 · -0.02em. Section headings. */
  val h2: TextStyle,
  /** Literata · 17 / 400 · -0.01em. Body, large variant. */
  val bodyLg: TextStyle,
  /** Literata · 15 / 400 · -0.01em. Default body. */
  val body: TextStyle,
  /** Geist Mono · 11 / 500 · 0.22em · UPPERCASE — chip text, structural labels. */
  val label: TextStyle,
  /** Geist Mono · 10 / 500 · 0.18em · UPPERCASE — eyebrows, table headers. */
  val caption: TextStyle,
  /** Geist Mono · 22 / 500 · -0.02em — large numeric values inline with prose. */
  val monoNum: TextStyle,
)

val flowFinTypography = FlowFinTypography(
  hero = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Light,
    fontSize = 56.sp,
    letterSpacing = (-0.04).em,
    lineHeight = 56.sp,
  ),
  display = TextStyle(
    fontFamily = Literata,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Normal,
    fontSize = 48.sp,
    letterSpacing = (-0.03).em,
    lineHeight = 48.sp,
  ),
  h1 = TextStyle(
    fontFamily = Literata,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    letterSpacing = (-0.02).em,
  ),
  h2 = TextStyle(
    fontFamily = Literata,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    letterSpacing = (-0.02).em,
  ),
  bodyLg = TextStyle(
    fontFamily = Literata,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    letterSpacing = (-0.01).em,
  ),
  body = TextStyle(
    fontFamily = Literata,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    letterSpacing = (-0.01).em,
  ),
  label = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    letterSpacing = 0.22.em,
  ),
  caption = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    letterSpacing = 0.18.em,
  ),
  monoNum = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Medium,
    fontSize = 22.sp,
    letterSpacing = (-0.02).em,
  ),
)

internal val LocalFlowFinTypography = compositionLocalOf<FlowFinTypography> {
  error("FlowFinTypography not provided; wrap content in FlowFinTheme.")
}
