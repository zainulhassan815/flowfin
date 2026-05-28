package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

/** How urgently a pending payment reads in its meta line. [Due] picks up the
 *  warning tint, [Late] the negative tint. */
enum class PaymentUrgency { Due, Late }

/** The colored status that trails the schedule in a [FlowFinPendingPaymentCard]
 *  meta line, e.g. "3 days late" ([Late]) or "Due today" ([Due]). */
data class PaymentStatus(val text: String, val urgency: PaymentUrgency)

/**
 * A due/overdue recurring payment with inline actions. The top row mirrors the
 * account card — category tile, name, amount — but the meta line carries the
 * schedule and an optional colored [status], and a divider separates a Skip /
 * Mark Paid action row below.
 */
@Composable
fun FlowFinPendingPaymentCard(
  icon: ImageVector,
  name: String,
  schedule: String,
  amount: String,
  onSkip: () -> Unit,
  onPay: () -> Unit,
  modifier: Modifier = Modifier,
  decimal: String? = null,
  tint: Color? = null,
  status: PaymentStatus? = null,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)

  Column(
    modifier = modifier
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 12.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      FlowFinTileIcon(icon = icon, tint = tint)

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = name,
          style = FlowFinTheme.typography.bodyLg,
          color = palette.text,
        )
        Row(
          modifier = Modifier.padding(top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = schedule,
            style = FlowFinTheme.typography.caption.copy(
              fontWeight = FontWeight.Normal,
              letterSpacing = 0.06.em,
            ),
            color = palette.textMute,
          )
          if (status != null) {
            Box(
              modifier = Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(palette.textFaint),
            )
            Text(
              text = status.text,
              style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.06.em),
              color = when (status.urgency) {
                PaymentUrgency.Due -> palette.warning
                PaymentUrgency.Late -> palette.negative
              },
            )
          }
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
        style = FlowFinTheme.typography.monoNum.copy(
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
        ),
        color = palette.text,
      )
    }

    Row(
      modifier = Modifier
        .padding(top = 10.dp)
        .fillMaxWidth()
        .drawBehind {
          val stroke = 1.dp.toPx()
          drawRect(color = palette.border, topLeft = Offset(0f, 0f), size = Size(size.width, stroke))
        }
        .padding(top = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      SkipButton(label = "Skip", onClick = onSkip)
      PayButton(label = "Mark Paid", onClick = onPay)
    }
  }
}

@Composable
private fun RowScope.SkipButton(label: String, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(10.dp)
  Box(
    modifier = Modifier
      .weight(1f)
      .height(38.dp)
      .clip(shape)
      .border(1.dp, palette.borderStrong, shape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
      color = palette.textMute,
    )
  }
}

@Composable
private fun RowScope.PayButton(label: String, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(10.dp)
  Row(
    modifier = Modifier
      .weight(1.4f)
      .height(38.dp)
      .clip(shape)
      .background(palette.accent)
      .clickable(onClick = onClick),
    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
      color = palette.bg,
    )
    Icon(
      imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
      contentDescription = null,
      tint = palette.bg,
      modifier = Modifier.size(14.dp),
    )
  }
}

@Preview(name = "Pending payment card", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewPendingPaymentCard() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .padding(24.dp)
      .fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    FlowFinPendingPaymentCard(
      icon = Icons.Rounded.Subscriptions,
      name = "Netflix",
      schedule = "Monthly · 22nd",
      amount = "1,500",
      decimal = ".00",
      tint = palette.categories.subs,
      status = PaymentStatus("3 days late", PaymentUrgency.Late),
      onSkip = {},
      onPay = {},
    )
    FlowFinPendingPaymentCard(
      icon = Icons.Rounded.Subscriptions,
      name = "Spotify",
      schedule = "Monthly · 1st",
      amount = "500",
      decimal = ".00",
      tint = palette.categories.fun_,
      status = PaymentStatus("Due today", PaymentUrgency.Due),
      onSkip = {},
      onPay = {},
    )
  }
}
