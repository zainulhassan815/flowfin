package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Restaurant
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
import kotlin.math.roundToInt

/** Spend framing for a budget account card: how much of the allocation is
 *  used. [fraction] (0..1) drives the bar; [spent] / [total] are the
 *  caller-formatted figures shown above it. */
data class BudgetProgress(
  val spent: String,
  val total: String,
  val fraction: Float,
)

/**
 * The account card on Home and the accounts list. A leading category tile, the
 * account name with a mono meta line, and the balance on the right.
 *
 * A real account passes a solid icon and no [progress]. A budget account sets
 * [dashedIcon] (the envelope treatment) and a [progress] block, which adds a
 * divider, a "spent of total" line, and a [FlowFinProgressBar] tinted to match.
 */
@Composable
fun FlowFinAccountCard(
  icon: ImageVector,
  name: String,
  meta: String,
  balance: String,
  modifier: Modifier = Modifier,
  decimal: String? = null,
  tint: Color? = null,
  dashedIcon: Boolean = false,
  progress: BudgetProgress? = null,
  onClick: (() -> Unit)? = null,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)

  Column(
    modifier = modifier
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
      .padding(14.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      FlowFinTileIcon(icon = icon, tint = tint, dashed = dashedIcon)

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = name,
          style = FlowFinTheme.typography.bodyLg,
          color = palette.text,
        )
        Text(
          text = meta.uppercase(),
          modifier = Modifier.padding(top = 4.dp),
          style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.12.em),
          color = palette.textMute,
        )
      }

      Text(
        text = buildAnnotatedString {
          append(balance)
          if (decimal != null) {
            withStyle(SpanStyle(color = palette.textMute, fontWeight = FontWeight.Normal)) {
              append(decimal)
            }
          }
        },
        style = FlowFinTheme.typography.monoNum.copy(
          fontSize = 17.sp,
          fontWeight = FontWeight.Medium,
        ),
        color = palette.text,
      )
    }

    if (progress != null) {
      BudgetProgressSection(progress = progress, tint = tint ?: palette.accent)
    }
  }
}

@Composable
private fun BudgetProgressSection(progress: BudgetProgress, tint: Color) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .padding(top = 12.dp)
      .fillMaxWidth()
      .drawBehind {
        val stroke = 1.dp.toPx()
        drawRect(color = palette.border, topLeft = Offset(0f, 0f), size = Size(size.width, stroke))
      }
      .padding(top = 12.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = buildAnnotatedString {
          withStyle(SpanStyle(color = palette.text, fontWeight = FontWeight.Medium)) {
            append(progress.spent)
          }
          append(" of ${progress.total} spent")
        },
        style = FlowFinTheme.typography.monoNum.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.Normal,
          letterSpacing = 0.em,
        ),
        color = palette.textMute,
      )
      Text(
        text = "${(progress.fraction * 100).roundToInt()}%",
        style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.1.em),
        color = palette.textSoft,
      )
    }
    Spacer(Modifier.height(8.dp))
    FlowFinProgressBar(progress = progress.fraction, color = tint)
  }
}

@Preview(name = "Account cards", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewAccountCards() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .padding(24.dp)
      .fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    FlowFinAccountCard(
      icon = Icons.Rounded.AccountBalance,
      name = "Bank",
      meta = "HBL · Primary",
      balance = "40,000",
      decimal = ".00",
      tint = palette.categories.bank,
    )
    FlowFinAccountCard(
      icon = Icons.Rounded.Restaurant,
      name = "Food",
      meta = "Monthly · Dec",
      balance = "9,000",
      decimal = ".00",
      tint = palette.categories.food,
      dashedIcon = true,
      progress = BudgetProgress(spent = "6,000", total = "15,000", fraction = 0.4f),
    )
  }
}
