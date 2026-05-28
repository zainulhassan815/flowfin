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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/** The semantic flavor of an insight, tinting its glyph, eyebrow, and the
 *  emphasized phrase in the title. */
enum class InsightTone { Warn, Positive, Negative, Info, Ai }

/**
 * A single dashboard insight. A tone-tinted glyph tile sits beside an eyebrow,
 * a serif title, and an optional mono meta line. Stack several inside a surface
 * container separated by `HorizontalDivider(color = FlowFinTheme.colors.border)`.
 *
 * [glyph] is a short symbol or emoji ("!", "✓", "✨"). [emphasis], when given,
 * is the substring of [title] to italicize and tint with the tone — so the
 * caller writes plain text and the component owns the highlight styling.
 */
@Composable
fun FlowFinInsightCard(
  tone: InsightTone,
  glyph: String,
  eyebrow: String,
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  emphasis: String? = null,
  meta: String? = null,
) {
  val palette = FlowFinTheme.colors
  val toneColor = when (tone) {
    InsightTone.Warn -> palette.warning
    InsightTone.Positive -> palette.positive
    InsightTone.Negative -> palette.negative
    InsightTone.Info -> palette.transfer
    InsightTone.Ai -> palette.accent
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalAlignment = Alignment.Top,
  ) {
    val glyphShape = RoundedCornerShape(10.dp)
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(glyphShape)
        .background(toneColor.copy(alpha = 0.08f))
        .border(1.dp, toneColor.copy(alpha = 0.28f), glyphShape),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = glyph,
        style = FlowFinTheme.typography.bodyLg.copy(fontSize = 16.sp),
        color = toneColor,
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = eyebrow.uppercase(),
        style = FlowFinTheme.typography.caption.copy(
          fontSize = 9.sp,
          letterSpacing = 0.22.em,
        ),
        color = toneColor,
      )
      Spacer(Modifier.height(5.dp))
      Text(
        text = emphasizedTitle(title, emphasis, toneColor),
        style = FlowFinTheme.typography.body.copy(
          fontSize = 15.5.sp,
          lineHeight = 21.sp,
        ),
        color = palette.text,
      )
      if (meta != null) {
        Spacer(Modifier.height(8.dp))
        Text(
          text = meta,
          style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.1.em),
          color = palette.textSoft,
        )
      }
    }
  }
}

private fun emphasizedTitle(title: String, emphasis: String?, toneColor: Color) =
  buildAnnotatedString {
    val start = emphasis?.let { title.indexOf(it) } ?: -1
    if (emphasis == null || start < 0) {
      append(title)
    } else {
      append(title.substring(0, start))
      withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = toneColor)) {
        append(emphasis)
      }
      append(title.substring(start + emphasis.length))
    }
  }

@Preview(name = "Insight cards", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewInsightCards() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .padding(24.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(palette.surface)
      .border(1.dp, palette.border, RoundedCornerShape(14.dp))
      .padding(horizontal = 16.dp),
  ) {
    FlowFinInsightCard(
      tone = InsightTone.Warn,
      glyph = "!",
      eyebrow = "Spending alert",
      title = "You've spent 40% more on Food than usual this month.",
      emphasis = "40% more on Food",
      meta = "Dec 1 – 27 · 22 transactions",
      onClick = {},
    )
    HorizontalDivider(color = palette.border)
    FlowFinInsightCard(
      tone = InsightTone.Positive,
      glyph = "✓",
      eyebrow = "On track",
      title = "Transport is under budget by Rs 1,200 — your slowest month all year.",
      emphasis = "under budget by Rs 1,200",
      meta = "vs Rs 3,400 average",
      onClick = {},
    )
    HorizontalDivider(color = palette.border)
    FlowFinInsightCard(
      tone = InsightTone.Ai,
      glyph = "✨",
      eyebrow = "Pattern detected",
      title = "Most of your Sunday spend goes to subscriptions and dining out.",
      emphasis = "Sunday spend",
      meta = "12 weeks observed",
      onClick = {},
    )
  }
}
