package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SwapHoriz
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

/** Which way money moved, which sets the amount color: [Expense] reads neutral
 *  (the leading `−` carries the meaning), [Income] positive, [Transfer] blue. */
enum class TransactionKind { Expense, Income, Transfer }

/**
 * A single transaction in a list. A small borderless tinted icon, the name
 * with a mono category/account meta line, and the signed amount on the right.
 *
 * The caller formats [amount] with its own sign (e.g. "−500", "+150,000"); the
 * color comes from [kind]. [tint] colors the icon — pass the category color for
 * an expense; income and transfer fall back to their semantic hues.
 */
@Composable
fun FlowFinTransactionRow(
  icon: ImageVector,
  name: String,
  meta: String,
  amount: String,
  kind: TransactionKind,
  modifier: Modifier = Modifier,
  decimal: String? = null,
  tint: Color? = null,
  onClick: (() -> Unit)? = null,
) {
  val palette = FlowFinTheme.colors

  val amountColor = when (kind) {
    TransactionKind.Expense -> palette.text
    TransactionKind.Income -> palette.positive
    TransactionKind.Transfer -> palette.transfer
  }
  val iconTint = tint ?: when (kind) {
    TransactionKind.Expense -> palette.textMute
    TransactionKind.Income -> palette.positive
    TransactionKind.Transfer -> palette.transfer
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(iconTint.copy(alpha = 0.10f)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(16.dp),
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name,
        style = FlowFinTheme.typography.bodyLg.copy(fontSize = 16.sp),
        color = palette.text,
      )
      if (meta.isNotBlank()) {
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
      style = FlowFinTheme.typography.monoNum.copy(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
      ),
      color = amountColor,
    )
  }
}

@Preview(name = "Transaction rows", backgroundColor = 0xFF101013, showBackground = true)
@Composable
private fun PreviewTransactionRows() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
    FlowFinTransactionRow(
      icon = Icons.Rounded.Restaurant,
      name = "Mess Lunch",
      meta = "Food · Dining",
      amount = "−500",
      decimal = ".00",
      kind = TransactionKind.Expense,
      tint = palette.categories.food,
    )
    FlowFinTransactionRow(
      icon = Icons.Rounded.SwapHoriz,
      name = "ATM Withdrawal",
      meta = "Bank → Cash",
      amount = "5,000",
      decimal = ".00",
      kind = TransactionKind.Transfer,
    )
    FlowFinTransactionRow(
      icon = Icons.Rounded.ArrowUpward,
      name = "December Salary",
      meta = "Bank · Salary",
      amount = "+150,000",
      decimal = ".00",
      kind = TransactionKind.Income,
    )
  }
}
