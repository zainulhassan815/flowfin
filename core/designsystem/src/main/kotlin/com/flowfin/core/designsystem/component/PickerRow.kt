package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * A selectable row inside picker sheets (account / category / person). A tile
 * icon, the name with an optional mono meta line, an optional trailing amount,
 * and a circular check on the right. When [selected] the row takes a soft cream
 * wash and the check fills with the accent.
 */
@Composable
fun FlowFinPickerRow(
  icon: ImageVector,
  name: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  meta: String? = null,
  amount: String? = null,
  tint: Color? = null,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(10.dp)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .then(if (selected) Modifier.background(palette.accent.copy(alpha = 0.06f)) else Modifier)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    FlowFinTileIcon(icon = icon, tint = tint, size = 36.dp)

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name,
        style = FlowFinTheme.typography.bodyLg.copy(fontSize = 16.sp),
        color = palette.text,
      )
      if (meta != null) {
        Text(
          text = meta.uppercase(),
          modifier = Modifier.padding(top = 4.dp),
          style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.12.em),
          color = palette.textMute,
        )
      }
    }

    if (amount != null) {
      Text(
        text = amount,
        modifier = Modifier.padding(end = 4.dp),
        style = FlowFinTheme.typography.monoNum.copy(
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
        ),
        color = palette.text,
      )
    }

    RowCheck(selected = selected)
  }
}

@Composable
private fun RowCheck(selected: Boolean) {
  val palette = FlowFinTheme.colors
  Box(
    modifier = Modifier
      .size(22.dp)
      .clip(CircleShape)
      .then(if (selected) Modifier.background(palette.accent) else Modifier)
      .border(
        width = 1.dp,
        color = if (selected) palette.accent else palette.borderStrong,
        shape = CircleShape,
      ),
    contentAlignment = Alignment.Center,
  ) {
    if (selected) {
      Icon(
        imageVector = Icons.Rounded.Check,
        contentDescription = null,
        tint = palette.bg,
        modifier = Modifier.size(12.dp),
      )
    }
  }
}

@Preview(name = "Picker rows", backgroundColor = 0xFF101013, showBackground = true)
@Composable
private fun PreviewPickerRows() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(modifier = Modifier.padding(16.dp)) {
    FlowFinPickerRow(
      icon = Icons.Rounded.AccountBalance,
      name = "Bank",
      meta = "HBL · Primary",
      amount = "40,000",
      tint = palette.categories.bank,
      selected = true,
      onClick = {},
    )
    FlowFinPickerRow(
      icon = Icons.Rounded.AccountBalanceWallet,
      name = "Cash",
      meta = "Wallet",
      amount = "7,000",
      tint = palette.categories.cash,
      selected = false,
      onClick = {},
    )
  }
}
