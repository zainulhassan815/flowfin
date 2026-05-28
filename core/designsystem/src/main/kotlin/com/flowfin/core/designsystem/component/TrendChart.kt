package com.flowfin.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * One bar in a [FlowFinTrendChart]. [value] is the height as a 0..1 fraction of
 * the chart; [color] fills it. [today] draws an accent tick below the baseline,
 * and [future] renders a dashed empty placeholder instead of a solid bar (for
 * periods not yet reached).
 */
data class TrendBar(
  val value: Float,
  val color: Color = Color.Unspecified,
  val today: Boolean = false,
  val future: Boolean = false,
)

/**
 * Bar chart for Reports — serves both the daily-spend trend (many thin bars)
 * and the monthly history (a few wide bars). Top-rounded bars grow from a faint
 * baseline; future periods show as dashed stubs and the current one gets an
 * accent tick beneath it.
 *
 * Bars are sized relative to the chart height, so the caller normalizes [value]
 * to 0..1 against whatever scale it wants (peak spend, a round ceiling, …).
 * [averageLine] (also 0..1) draws a dashed reference line — an average or
 * threshold — with an optional [averageLabel] pinned to its right end.
 * [barCorner] tunes the top-corner radius (small for thin bars, larger for
 * wide ones).
 */
@Composable
fun FlowFinTrendChart(
  bars: List<TrendBar>,
  modifier: Modifier = Modifier,
  averageLine: Float? = null,
  averageLabel: String? = null,
  barCorner: Dp = 2.dp,
) {
  val palette = FlowFinTheme.colors
  val baselineColor = Color.White.copy(alpha = 0.06f)
  val futureColor = Color.White.copy(alpha = 0.07f)
  val accent = palette.accent
  val referenceColor = palette.textFaint
  val measurer = rememberTextMeasurer()
  val labelStyle = FlowFinTheme.typography.caption.copy(
    fontSize = 9.sp,
    letterSpacing = 0.1.em,
    color = palette.textSoft,
  )

  Canvas(modifier = modifier.fillMaxWidth().height(86.dp)) {
    val count = bars.size.coerceAtLeast(1)
    val slot = size.width / count
    val barWidth = slot * 0.7f
    val baselineY = size.height - 6.dp.toPx()
    val cornerPx = barCorner.toPx()

    drawLine(
      color = baselineColor,
      start = Offset(0f, baselineY),
      end = Offset(size.width, baselineY),
      strokeWidth = 0.6.dp.toPx(),
    )

    if (averageLine != null) {
      val y = baselineY - averageLine.coerceIn(0f, 1f) * baselineY
      drawLine(
        color = referenceColor,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())),
      )
    }

    bars.forEachIndexed { index, bar ->
      val centerX = slot * index + slot / 2f
      val left = centerX - barWidth / 2f

      if (bar.future) {
        val height = 6.dp.toPx()
        drawRoundRect(
          color = futureColor,
          topLeft = Offset(left, baselineY - height),
          size = Size(barWidth, height),
          cornerRadius = CornerRadius(1.5.dp.toPx()),
          style = Stroke(
            width = 0.6.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.dp.toPx(), 1.5.dp.toPx())),
          ),
        )
      } else {
        val height = bar.value.coerceIn(0f, 1f) * baselineY
        drawTopRoundedBar(left, baselineY - height, barWidth, height, cornerPx, bar.color)
        if (bar.today) {
          drawLine(
            color = accent,
            start = Offset(centerX, baselineY),
            end = Offset(centerX, size.height),
            strokeWidth = 1.dp.toPx(),
          )
        }
      }
    }

    if (averageLine != null && averageLabel != null) {
      val y = baselineY - averageLine.coerceIn(0f, 1f) * baselineY
      val measured = measurer.measure(averageLabel, labelStyle)
      drawText(
        textLayoutResult = measured,
        topLeft = Offset(
          x = size.width - measured.size.width,
          y = y - measured.size.height - 2.dp.toPx(),
        ),
      )
    }
  }
}

/** A bar with only its top corners rounded, so it sits flush on the baseline. */
private fun DrawScope.drawTopRoundedBar(
  left: Float,
  top: Float,
  width: Float,
  height: Float,
  radius: Float,
  color: Color,
) {
  if (height <= 0f) return
  val r = radius.coerceAtMost(width / 2f).coerceAtMost(height)
  val path = Path().apply {
    addRoundRect(
      RoundRect(
        rect = Rect(left, top, left + width, top + height),
        topLeft = CornerRadius(r, r),
        topRight = CornerRadius(r, r),
        bottomLeft = CornerRadius.Zero,
        bottomRight = CornerRadius.Zero,
      ),
    )
  }
  drawPath(path, color)
}

@Preview(name = "Trend · daily spend", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 360)
@Composable
private fun PreviewTrendChartDaily() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  val categories = palette.categories
  val food = categories.food.copy(alpha = 0.55f)

  val past = listOf(
    0.15f, 0.25f, 0.20f, 0.30f, 0.38f, 0.50f, 0.82f, 0.28f, 0.22f, 0.30f,
    0.52f, 0.25f, 0.32f, 0.42f, 1.0f, 0.28f, 0.30f, 0.26f, 0.22f, 0.34f,
    0.40f, 0.30f, 0.28f, 0.36f, 0.24f, 0.30f,
  )
  val bars = buildList {
    past.forEachIndexed { index, value ->
      val color = when (index) {
        6 -> palette.warning
        14 -> categories.rent
        else -> food
      }
      add(TrendBar(value = value, color = color))
    }
    add(TrendBar(value = 0.25f, color = palette.accent, today = true))
    repeat(4) { add(TrendBar(value = 0f, future = true)) }
  }

  ChartCard {
    FlowFinTrendChart(bars = bars)
    ChartAxis("Dec 1", "Today · 27", "31")
  }
}

@Preview(name = "Trend · income history", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 360)
@Composable
private fun PreviewTrendChartMonthly() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  val month = palette.positive.copy(alpha = 0.5f)
  val bars = listOf(
    TrendBar(0.55f, month),
    TrendBar(0.65f, month),
    TrendBar(0.50f, month),
    TrendBar(0.80f, month),
    TrendBar(0.58f, month),
    TrendBar(0.72f, palette.accent, today = true),
  )

  ChartCard {
    FlowFinTrendChart(
      bars = bars,
      averageLine = 0.64f,
      averageLabel = "144K",
      barCorner = 6.dp,
    )
    ChartAxis("Jul", "", "Dec")
  }
}

@Composable
private fun ChartCard(content: @Composable ColumnScope.() -> Unit) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .padding(24.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(palette.surface)
      .border(1.dp, palette.border, RoundedCornerShape(12.dp))
      .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 10.dp),
    content = content,
  )
}

@Composable
private fun ChartAxis(start: String, middle: String, end: String) {
  val palette = FlowFinTheme.colors
  val axisStyle = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.14.em)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(start, style = axisStyle, color = palette.textSoft)
    Text(middle, style = axisStyle, color = palette.accent)
    Text(end, style = axisStyle, color = palette.textSoft)
  }
}
