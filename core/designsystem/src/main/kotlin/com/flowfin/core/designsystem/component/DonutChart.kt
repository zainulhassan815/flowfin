package com.flowfin.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/** One slice of a [FlowFinDonutChart]: a [value] (any unit — slices are sized
 *  relative to the total) and the [color] to draw it in. */
data class DonutSegment(val value: Float, val color: Color)

/**
 * Category-breakdown donut. Draws a faint full-ring track with the [segments]
 * laid contiguously over it, starting at twelve o'clock and going clockwise,
 * each sized as its share of the total. The [content] slot sits centered in the
 * hole — typically a label and the running total.
 *
 * Size it through [modifier] (e.g. `Modifier.size(180.dp)`); the ring scales to
 * fill the given square.
 */
@Composable
fun FlowFinDonutChart(
  segments: List<DonutSegment>,
  modifier: Modifier = Modifier,
  trackColor: Color = Color.White.copy(alpha = 0.04f),
  content: @Composable BoxScope.() -> Unit = {},
) {
  val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val dim = size.minDimension
      val arcDim = dim * 0.80f
      val topLeft = Offset((size.width - arcDim) / 2f, (size.height - arcDim) / 2f)
      val arcSize = Size(arcDim, arcDim)
      val stroke = Stroke(width = dim * 0.14f, cap = StrokeCap.Butt)

      drawArc(
        color = trackColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = stroke,
      )

      var start = -90f
      segments.forEach { segment ->
        val sweep = 360f * segment.value / total
        drawArc(
          color = segment.color,
          startAngle = start,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = stroke,
        )
        start += sweep
      }
    }
    content()
  }
}

@Preview(name = "Donut chart", backgroundColor = 0xFF101013, showBackground = true)
@Composable
private fun PreviewDonutChart() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  val categories = palette.categories
  Box(modifier = Modifier.padding(24.dp)) {
    FlowFinDonutChart(
      segments = listOf(
        DonutSegment(16000f, categories.food),
        DonutSegment(12000f, categories.rent),
        DonutSegment(7000f, categories.transport),
        DonutSegment(6000f, categories.utilities),
        DonutSegment(4000f, categories.subs),
      ),
      modifier = Modifier.size(180.dp),
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "TOTAL SPENT",
          style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.22.em),
          color = palette.textMute,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Rs",
            modifier = Modifier.alignByBaseline(),
            style = FlowFinTheme.typography.h2.copy(fontSize = 13.sp),
            color = palette.textMute,
          )
          Spacer(Modifier.size(2.dp))
          Text(
            text = "45,000",
            modifier = Modifier.alignByBaseline(),
            style = FlowFinTheme.typography.monoNum.copy(fontSize = 22.sp),
            color = palette.text,
          )
        }
      }
    }
  }
}
