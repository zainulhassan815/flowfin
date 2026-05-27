package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Small inline labels that sit beside body text. Three shapes for three jobs:
 *
 * - [StatusTag] — urgency / state (Due, Late, Settled, Pending, …).
 * - [FrequencyTag] — neutral metadata (Monthly, Weekly, EVERY 1st).
 * - [CountBadge] — numeric indicators (unread count, item count).
 */

/** Semantic tones for [StatusTag]. */
enum class StatusTone { Warning, Critical, Positive, Info, Neutral }

/**
 * Colored pill carrying a status like "Due today" or "3 days late". The tone
 * picks the foreground color; the background is a low-alpha tint of the same
 * color so the pill reads as belonging to the same hue family.
 */
@Composable
fun StatusTag(
  text: String,
  tone: StatusTone,
  modifier: Modifier = Modifier,
) {
  val (fg, bg) = tone.colors()
  Pill(
    text = text.uppercase(),
    foreground = fg,
    background = bg,
    shape = RoundedCornerShape(6.dp),
    horizontalPadding = 9.dp,
    verticalPadding = 4.dp,
    textStyle = FlowFinTheme.typography.caption.copy(letterSpacing = 0.12.em),
    modifier = modifier,
  )
}

/**
 * Neutral metadata pill for cadence and similar lightweight tags — quieter
 * than a status, used inline with content (e.g. on a recurring schedule row).
 */
@Composable
fun FrequencyTag(
  text: String,
  modifier: Modifier = Modifier,
) {
  Pill(
    text = text.uppercase(),
    foreground = FlowFinTheme.colors.textSoft,
    background = Color.White.copy(alpha = 0.05f),
    shape = RoundedCornerShape(5.dp),
    horizontalPadding = 7.dp,
    verticalPadding = 3.dp,
    textStyle = FlowFinTheme.typography.caption.copy(
      letterSpacing = 0.16.em,
      fontSize = 9.sp,
    ),
    modifier = modifier,
  )
}

/**
 * Numeric indicator — unread payments, pending notifications, list counts.
 * Renders the number as monospace digits with a subtle surface tint.
 */
@Composable
fun CountBadge(
  count: Int,
  modifier: Modifier = Modifier,
) {
  Pill(
    text = count.toString(),
    foreground = FlowFinTheme.colors.text,
    background = Color.White.copy(alpha = 0.06f),
    shape = RoundedCornerShape(8.dp),
    horizontalPadding = 7.dp,
    verticalPadding = 2.dp,
    textStyle = FlowFinTheme.typography.caption.copy(letterSpacing = 0.em),
    modifier = modifier,
  )
}

@Composable
private fun StatusTone.colors(): Pair<Color, Color> {
  val palette = FlowFinTheme.colors
  return when (this) {
    StatusTone.Warning  -> palette.warning  to palette.warning.copy(alpha = 0.08f)
    StatusTone.Critical -> palette.negative to palette.negative.copy(alpha = 0.08f)
    StatusTone.Positive -> palette.positive to palette.positive.copy(alpha = 0.08f)
    StatusTone.Info     -> palette.transfer to palette.transfer.copy(alpha = 0.08f)
    StatusTone.Neutral  -> palette.textSoft to Color.White.copy(alpha = 0.05f)
  }
}

@Composable
private fun Pill(
  text: String,
  foreground: Color,
  background: Color,
  shape: androidx.compose.ui.graphics.Shape,
  horizontalPadding: Dp,
  verticalPadding: Dp,
  textStyle: TextStyle,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    color = foreground,
    style = textStyle,
    modifier = modifier
      .clip(shape)
      .background(background)
      .padding(horizontal = horizontalPadding, vertical = verticalPadding),
  )
}

@Preview(name = "Status tags", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewStatusTags() = FlowFinTheme {
  Row(
    modifier = Modifier.padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    StatusTag("Due today", StatusTone.Warning)
    StatusTag("3 days late", StatusTone.Critical)
    StatusTag("Settled", StatusTone.Positive)
    StatusTag("Pending", StatusTone.Info)
    StatusTag("Skipped", StatusTone.Neutral)
  }
}

@Preview(name = "Frequency tags", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewFrequencyTags() = FlowFinTheme {
  Row(
    modifier = Modifier.padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    FrequencyTag("Monthly")
    FrequencyTag("Weekly")
    FrequencyTag("Yearly")
  }
}

@Preview(name = "Count badges", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewCountBadges() = FlowFinTheme {
  Row(
    modifier = Modifier.padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CountBadge(2)
    CountBadge(12)
    CountBadge(99)
  }
}
