package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

private val RailX = 5.5.dp
private val DotSize = 11.dp
private val LeadWidth = 18.dp

/** The flavor of a timeline entry: a [Payment] (positive) or the [Origin]
 *  borrowing event that started the debt (warning). Sets the dot, the amount
 *  color, and whether the name reads in soft italic. */
enum class TimelineTone { Payment, Origin }

/**
 * Vertical payment-history timeline — a solid rail with a dot per entry. Place
 * [FlowFinTimelineItem]s inside, and optionally a [FlowFinTimelineEmptyItem] at
 * the top when a debt has no payments yet. The rail and inter-item spacing are
 * handled here; items supply their own date, label, and amount.
 */
@Composable
fun FlowFinTimeline(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = modifier.drawBehind {
      val x = RailX.toPx()
      val inset = 6.dp.toPx()
      drawLine(
        color = palette.borderStrong,
        start = Offset(x, inset),
        end = Offset(x, size.height - inset),
        strokeWidth = 1.dp.toPx(),
      )
    },
    verticalArrangement = Arrangement.spacedBy(18.dp),
    content = content,
  )
}

/**
 * One timeline entry: an uppercase mono [date], then a baseline row of the
 * serif [name] (italic for an [TimelineTone.Origin]) with an optional mono
 * [meta] line, and the [amount] aligned right. [decimal], when set, trails the
 * amount in a quieter weight.
 */
@Composable
fun FlowFinTimelineItem(
  date: String,
  name: String,
  amount: String,
  tone: TimelineTone,
  modifier: Modifier = Modifier,
  meta: String? = null,
  decimal: String? = null,
) {
  val palette = FlowFinTheme.colors
  val toneColor = when (tone) {
    TimelineTone.Payment -> palette.positive
    TimelineTone.Origin -> palette.warning
  }

  Row(modifier = modifier.fillMaxWidth().padding(top = 4.dp)) {
    Lead {
      when (tone) {
        TimelineTone.Payment -> Dot(borderColor = palette.positive, fillColor = palette.positive.copy(alpha = 0.16f))
        TimelineTone.Origin -> Dot(borderColor = palette.warning, fillColor = palette.warning)
      }
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = date.uppercase(),
        style = FlowFinTheme.typography.caption.copy(fontSize = 9.5.sp, letterSpacing = 0.18.em),
        color = palette.textSoft,
      )
      Spacer(Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = name,
            style = FlowFinTheme.typography.body.copy(
              fontSize = 16.sp,
              fontStyle = if (tone == TimelineTone.Origin) FontStyle.Italic else FontStyle.Normal,
            ),
            color = if (tone == TimelineTone.Origin) palette.textMute else palette.text,
          )
          if (meta != null) {
            Text(
              text = meta.uppercase(),
              modifier = Modifier.padding(top = 4.dp),
              style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.08.em),
              color = palette.textMute,
            )
          }
        }
        Text(
          text = buildAnnotatedString {
            append(amount)
            if (decimal != null) {
              withStyle(SpanStyle(color = palette.textMute, fontWeight = FontWeight.Normal)) {
                append(decimal)
              }
            }
          },
          style = FlowFinTheme.typography.monoNum.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
          color = toneColor,
        )
      }
    }
  }
}

/**
 * The "no payments yet" placeholder, shown at the top of the timeline when a
 * debt has only its origin entry. A hollow dot rides the rail beside a dashed
 * card with an [eyebrow], an italic [title], and a CTA ([actionLabel]) that
 * fires [onAction] to record the first payment.
 */
@Composable
fun FlowFinTimelineEmptyItem(
  eyebrow: String,
  title: String,
  actionLabel: String,
  onAction: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  val cardShape = RoundedCornerShape(12.dp)

  Row(modifier = modifier.fillMaxWidth().padding(top = 4.dp)) {
    Lead {
      Dot(borderColor = palette.textFaint, fillColor = palette.bg) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(palette.textFaint.copy(alpha = 0.6f)),
        )
      }
    }

    Row(
      modifier = Modifier
        .weight(1f)
        .clip(cardShape)
        .drawBehind {
          val stroke = 1.dp.toPx()
          drawRoundRect(
            color = palette.borderStrong,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(
              width = stroke,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            ),
          )
        }
        .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = eyebrow.uppercase(),
          style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.24.em),
          color = palette.textFaint,
        )
        Spacer(Modifier.height(4.dp))
        Text(
          text = title,
          style = FlowFinTheme.typography.body.copy(fontSize = 15.5.sp, fontStyle = FontStyle.Italic),
          color = palette.text,
        )
      }

      val linkShape = RoundedCornerShape(9.dp)
      Row(
        modifier = Modifier
          .clip(linkShape)
          .border(1.dp, palette.accent.copy(alpha = 0.32f), linkShape)
          .clickable(onClick = onAction)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = actionLabel.uppercase(),
          style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.18.em, fontWeight = FontWeight.SemiBold),
          color = palette.accent,
        )
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = null,
          tint = palette.accent,
          modifier = Modifier.size(12.dp),
        )
      }
    }
  }
}

/** The fixed-width rail column that holds a dot, positioned to sit on the line. */
@Composable
private fun Lead(dot: @Composable () -> Unit) {
  Box(modifier = Modifier.width(LeadWidth)) {
    Box(modifier = Modifier.padding(top = 4.dp)) { dot() }
  }
}

@Composable
private fun Dot(
  borderColor: Color,
  fillColor: Color,
  content: @Composable BoxScope.() -> Unit = {},
) {
  Box(
    modifier = Modifier
      .size(DotSize)
      .clip(CircleShape)
      .background(fillColor)
      .border(1.5.dp, borderColor, CircleShape),
    contentAlignment = Alignment.Center,
    content = content,
  )
}

@Preview(name = "Timeline", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewTimeline() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(40.dp),
  ) {
    FlowFinTimeline {
      FlowFinTimelineEmptyItem(
        eyebrow = "Future payments",
        title = "No payments yet.",
        actionLabel = "Record one",
        onAction = {},
      )
      FlowFinTimelineItem(
        date = "Today · 27 May",
        name = "Borrowed from Ahmed",
        meta = "For rent · Original amount",
        amount = "+8,000",
        decimal = ".00",
        tone = TimelineTone.Origin,
      )
    }

    FlowFinTimeline {
      FlowFinTimelineItem(
        date = "Dec 25 · Thu",
        name = "Payment to Ahmed",
        meta = "via Bank",
        amount = "−1,500",
        decimal = ".00",
        tone = TimelineTone.Payment,
      )
      FlowFinTimelineItem(
        date = "Dec 10 · Wed",
        name = "Borrowed from Ahmed",
        meta = "For rent · Original amount",
        amount = "+8,000",
        decimal = ".00",
        tone = TimelineTone.Origin,
      )
    }
  }
}
