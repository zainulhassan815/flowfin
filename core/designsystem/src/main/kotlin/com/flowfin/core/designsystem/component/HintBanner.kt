package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/** The tint of a [FlowFinHintBanner] — cream [Accent] by default, [Warn] for a
 *  heads-up, [Info] for a neutral note. */
enum class HintTone { Accent, Warn, Info }

/**
 * A soft helper banner — onboarding tips, tooltips, gentle warnings. A
 * tone-tinted icon beside serif body text on a low-alpha wash of the same hue.
 *
 * [emphasis], when given, is the substring of [text] to brighten to full
 * contrast (e.g. a "Tip:" lead) while the rest stays muted.
 */
@Composable
fun FlowFinHintBanner(
  icon: ImageVector,
  text: String,
  modifier: Modifier = Modifier,
  emphasis: String? = null,
  tone: HintTone = HintTone.Accent,
) {
  val palette = FlowFinTheme.colors
  val toneColor = when (tone) {
    HintTone.Accent -> palette.accent
    HintTone.Warn -> palette.warning
    HintTone.Info -> palette.transfer
  }
  val shape = RoundedCornerShape(12.dp)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(toneColor.copy(alpha = 0.04f))
      .border(1.dp, toneColor.copy(alpha = 0.16f), shape)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = toneColor,
      modifier = Modifier.size(22.dp),
    )
    Text(
      text = buildAnnotatedString {
        val start = emphasis?.let { text.indexOf(it) } ?: -1
        if (emphasis == null || start < 0) {
          append(text)
        } else {
          append(text.substring(0, start))
          withStyle(SpanStyle(color = palette.text, fontWeight = FontWeight.Medium)) {
            append(emphasis)
          }
          append(text.substring(start + emphasis.length))
        }
      },
      style = FlowFinTheme.typography.body.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
      color = palette.textMute,
    )
  }
}

@Preview(name = "Hint banners", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewHintBanners() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    FlowFinHintBanner(
      icon = Icons.Rounded.Info,
      text = "Tip: Real accounts hold money. Budgets are how you'd like to spend it.",
      emphasis = "Tip:",
    )
    FlowFinHintBanner(
      icon = Icons.Rounded.Warning,
      text = "Heads up: Two budgets are over for the month.",
      emphasis = "Heads up:",
      tone = HintTone.Warn,
    )
  }
}
