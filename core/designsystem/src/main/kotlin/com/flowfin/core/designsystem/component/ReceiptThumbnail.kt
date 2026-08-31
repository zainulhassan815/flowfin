package com.flowfin.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Decorative receipt tile for the add-transaction attachment strip — a small
 * gradient "paper" with a few faint line rules and a scalloped, torn bottom
 * edge. Purely cosmetic; it stands in for an attached receipt rather than
 * rendering a real image.
 */
@Composable
fun FlowFinReceiptThumbnail(modifier: Modifier = Modifier) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(8.dp)

  Box(
    modifier = modifier
      .size(width = 48.dp, height = 60.dp)
      .clip(shape)
      .background(Brush.verticalGradient(listOf(palette.surface2, palette.surface))),
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val inset = 8.dp.toPx()
      val lineHeight = 1.dp.toPx()
      // Faux receipt lines. Tinted off the palette's faintest text so they read
      // on a light ground as well as a dark one.
      listOf(0.55f, 0.42f, 0.30f).forEachIndexed { index, alpha ->
        val y = (8 + index * 6).dp.toPx()
        drawRect(
          color = palette.textFaint.copy(alpha = alpha),
          topLeft = Offset(inset, y),
          size = Size(size.width - inset * 2, lineHeight),
        )
      }

      // Torn edge: backdrop-colored notches bitten up from the bottom.
      val radius = 3.dp.toPx()
      val spacing = 6.dp.toPx()
      var centerX = spacing / 2f
      while (centerX < size.width) {
        drawCircle(color = palette.bg, radius = radius, center = Offset(centerX, size.height))
        centerX += spacing
      }
    }
  }
}

@Preview(name = "Receipt thumbnail", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewReceiptThumbnail() = FlowFinTheme {
  Row(
    modifier = Modifier.padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    FlowFinReceiptThumbnail()
    FlowFinReceiptThumbnail()
    FlowFinReceiptThumbnail()
  }
}
