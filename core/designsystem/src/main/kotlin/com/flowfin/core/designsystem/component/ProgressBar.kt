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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
  trackColor: Color = Color.White.copy(alpha = 0.06f),
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
    FlowFinProgressBar(progress = 0.4f, color = palette.categories.food)
    FlowFinProgressBar(progress = 0.75f, color = palette.categories.transport)
    FlowFinProgressBar(progress = 1f, color = palette.categories.fun_)
  }
}
