package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.designsystem.theme.GeistMono

/** Tone for the not-enough-yet notices: cream [Accent] (default) or [Positive]
 *  for the "all caught up" good-news variant. */
enum class NoticeTone { Accent, Positive }

/**
 * **N — Not-enough-yet.** A calm informational banner for when the user can't
 * act yet, only wait — a tinted glyph beside an italic [title] (optional accent
 * [emphasis]) over a mono [sub] line. [tone] flips it to the positive "caught
 * up" treatment.
 */
@Composable
fun FlowFinNotEnoughBanner(
  icon: ImageVector,
  title: String,
  modifier: Modifier = Modifier,
  emphasis: String? = null,
  sub: String? = null,
  tone: NoticeTone = NoticeTone.Accent,
) {
  val palette = FlowFinTheme.colors
  val toneColor = when (tone) {
    NoticeTone.Accent -> palette.accent
    NoticeTone.Positive -> palette.positive
  }
  val shape = RoundedCornerShape(12.dp)
  val glyphShape = RoundedCornerShape(9.dp)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(toneColor.copy(alpha = 0.05f))
      .border(1.dp, toneColor.copy(alpha = 0.18f), shape)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(11.dp),
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(glyphShape)
        .background(toneColor.copy(alpha = 0.10f))
        .border(1.dp, toneColor.copy(alpha = 0.28f), glyphShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = toneColor, modifier = Modifier.size(16.dp))
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = emphasizedTitle(title, emphasis, toneColor),
        style = FlowFinTheme.typography.h2.copy(fontSize = 13.5.sp, letterSpacing = (-0.01).em),
        color = palette.text,
      )
      if (sub != null) {
        Text(
          text = sub.uppercase(),
          modifier = Modifier.padding(top = 4.dp),
          style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.18.em),
          color = palette.textSoft,
        )
      }
    }
  }
}

/**
 * **N — Progress pill.** The small "Day 5 / 28" capsule shown on not-enough-yet
 * surfaces: a slim accent bar over a faint track plus a mono [label].
 */
@Composable
fun FlowFinNotEnoughProgress(
  progress: Float,
  label: String,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = modifier
      .clip(CircleShape)
      .background(palette.accent.copy(alpha = 0.06f))
      .border(1.dp, palette.accent.copy(alpha = 0.20f), CircleShape)
      .padding(horizontal = 11.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(9.dp),
  ) {
    FlowFinProgressBar(
      progress = progress,
      modifier = Modifier.width(44.dp),
      color = palette.accent,
      height = 3.dp,
    )
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.caption.copy(
        fontSize = 8.5.sp,
        letterSpacing = 0.18.em,
        fontWeight = FontWeight.SemiBold,
      ),
      color = palette.accent,
    )
  }
}

/**
 * **Q — Filter chip.** A dismissable category pill: a [markerColor] dot, the
 * [label], and an ✕ that fires [onDismiss]. The pill tints to the marker color.
 */
@Composable
fun FlowFinFilterChip(
  label: String,
  markerColor: Color,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = modifier
      .clip(CircleShape)
      .background(markerColor.copy(alpha = 0.08f))
      .border(1.dp, markerColor.copy(alpha = 0.28f), CircleShape)
      .padding(start = 9.dp, top = 5.dp, end = 5.dp, bottom = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(7.dp),
  ) {
    Box(
      modifier = Modifier
        .size(5.dp)
        .clip(CircleShape)
        .background(markerColor),
    )
    Text(
      text = label,
      style = FlowFinTheme.typography.caption.copy(fontSize = 9.5.sp, letterSpacing = 0.06.em),
      color = palette.text,
    )
    Box(
      modifier = Modifier
        .size(16.dp)
        .clip(CircleShape)
        .background(Color.Black.copy(alpha = 0.25f))
        .clickable(onClick = onDismiss),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Rounded.Close,
        contentDescription = "Remove filter",
        tint = palette.textMute,
        modifier = Modifier.size(10.dp),
      )
    }
  }
}

/**
 * **Q — Filter / search empty.** A centered notice for "your query has no
 * results": an [eyebrow] chip echoing the query/period, a title that renders
 * the [query] as a mono accent term, and a primary action plus an optional
 * secondary one.
 */
@Composable
fun FlowFinQueryEmpty(
  eyebrow: String,
  title: String,
  primaryActionLabel: String,
  onPrimaryAction: () -> Unit,
  modifier: Modifier = Modifier,
  query: String? = null,
  queryColor: Color = FlowFinTheme.colors.accent,
  secondaryActionLabel: String? = null,
  onSecondaryAction: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = eyebrow,
      modifier = Modifier
        .clip(RoundedCornerShape(6.dp))
        .background(palette.accent.copy(alpha = 0.10f))
        .border(1.dp, palette.accent.copy(alpha = 0.28f), RoundedCornerShape(6.dp))
        .padding(horizontal = 7.dp, vertical = 2.dp),
      style = FlowFinTheme.typography.h2.copy(fontSize = 10.5.sp, letterSpacing = 0.em),
      color = palette.accent,
    )

    Spacer(Modifier.height(10.dp))
    Text(
      text = buildAnnotatedString {
        val start = query?.let { title.indexOf(it) } ?: -1
        if (query == null || start < 0) {
          append(title)
        } else {
          append(title.substring(0, start))
          withStyle(SpanStyle(fontFamily = GeistMono, color = queryColor, fontStyle = FontStyle.Normal)) {
            append(query)
          }
          append(title.substring(start + query.length))
        }
      },
      style = FlowFinTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 21.sp),
      color = palette.textMute,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      QueryLink(label = primaryActionLabel, onClick = onPrimaryAction, primary = true)
      if (secondaryActionLabel != null) {
        QueryLink(label = secondaryActionLabel, onClick = onSecondaryAction, primary = false)
      }
    }
  }
}

@Composable
private fun QueryLink(label: String, onClick: () -> Unit, primary: Boolean) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(8.dp)
  val color = if (primary) palette.accent else palette.textMute
  val borderColor = if (primary) palette.accent.copy(alpha = 0.32f) else palette.borderStrong
  Text(
    text = label.uppercase(),
    modifier = Modifier
      .clip(shape)
      .border(1.dp, borderColor, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.18.em, fontWeight = FontWeight.Medium),
    color = color,
  )
}

private fun emphasizedTitle(text: String, emphasis: String?, color: Color) =
  buildAnnotatedString {
    val start = emphasis?.let { text.indexOf(it) } ?: -1
    if (emphasis == null || start < 0) {
      append(text)
    } else {
      append(text.substring(0, start))
      withStyle(SpanStyle(color = color, fontStyle = FontStyle.Italic)) { append(emphasis) }
      append(text.substring(start + emphasis.length))
    }
  }

@Preview(name = "Empty · not-enough & query", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 360)
@Composable
private fun PreviewEmptyNotices() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FlowFinNotEnoughBanner(
      icon = Icons.Rounded.Schedule,
      title = "Building a picture — 5 days in.",
      emphasis = "5 days in.",
      sub = "Patterns sharpen after ~4 weeks",
    )
    FlowFinNotEnoughBanner(
      icon = Icons.Rounded.Check,
      title = "Nothing's due right now.",
      sub = "Next bill · Rent · 1 Jun",
      tone = NoticeTone.Positive,
    )
    FlowFinNotEnoughProgress(progress = 0.18f, label = "Day 5 / 28")
    FlowFinFilterChip(
      label = "Food & Dining",
      markerColor = palette.categories.food,
      onDismiss = {},
    )
    FlowFinQueryEmpty(
      eyebrow = "Food & Dining · May 2026",
      title = "Nothing matches Food in this period.",
      query = "Food",
      queryColor = palette.categories.food,
      primaryActionLabel = "Clear filter",
      onPrimaryAction = {},
      secondaryActionLabel = "Pick another",
      onSecondaryAction = {},
    )
  }
}
