package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Onboarding step indicator — a row of slim bars. Steps before [current] read
 * as a faded cream (done), the [current] step is solid accent, and the rest
 * stay muted.
 */
@Composable
fun FlowFinProgressDots(
  total: Int,
  current: Int,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(total) { index ->
      val color = when {
        index == current -> palette.accent
        index < current -> palette.accent.copy(alpha = 0.3f)
        else -> Color.White.copy(alpha = 0.10f)
      }
      Box(
        modifier = Modifier
          .size(width = 24.dp, height = 3.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(color),
      )
    }
  }
}

@Preview(name = "Progress dots", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewProgressDots() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    FlowFinProgressDots(total = 4, current = 0)
    FlowFinProgressDots(total = 4, current = 2)
    FlowFinProgressDots(total = 4, current = 3)
  }
}
