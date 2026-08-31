package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Thin linear progress bar — the budget spend indicator and any other
 * fraction-of-whole readout. [progress] is clamped to 0..1; [color] is the
 * fill (usually the category color) over a faint track.
 */
@Composable
fun FlowFinProgressBar(
  progress: Float,
  modifier: Modifier = Modifier,
  color: Color = FlowFinTheme.colors.accent,
  trackColor: Color = FlowFinTheme.colors.surface3,
  height: Dp = 5.dp,
) {
  val shape = RoundedCornerShape(3.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .clip(shape)
      .background(trackColor),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(progress.coerceIn(0f, 1f))
        .fillMaxHeight()
        .clip(shape)
        .background(color),
    )
  }
}

@Preview(name = "Progress bar", backgroundColor = 0xFF101013, showBackground = true)
@Composable
private fun PreviewProgressBar() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .padding(24.dp)
      .fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Bare bars in category colors, plus a warning fill for over-budget.
    FlowFinProgressBar(progress = 0.4f, color = palette.categories.food)
    FlowFinProgressBar(progress = 1f, color = palette.warning)

    // The optional "label · percent" framing screens compose above the bar.
    LabeledBar("Food", 0.4f, palette.categories.food)
    LabeledBar("Transport", 0.1f, palette.categories.transport)
    LabeledBar("Fun", 0.8f, palette.categories.fun_)
  }
}

@Composable
private fun LabeledBar(label: String, progress: Float, color: Color) {
  Column {
    Text(
      text = "$label · ${(progress * 100).toInt()}%",
      modifier = Modifier.padding(bottom = 6.dp),
      style = FlowFinTheme.typography.monoNum.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.04.em,
      ),
      color = FlowFinTheme.colors.textMute,
    )
    FlowFinProgressBar(progress = progress, color = color)
  }
}
