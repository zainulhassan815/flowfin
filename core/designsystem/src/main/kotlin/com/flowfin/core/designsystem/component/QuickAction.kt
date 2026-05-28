package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * One of Home's quick-action buttons — the +Income / +Expense / +Move trio. A
 * vertical card with a tinted icon tile, an uppercase mono [label], and an
 * italic serif [sub] hint.
 *
 * [tint] colors the icon to the action's semantic (positive for income,
 * negative for expense, transfer for a move). Lay three out in a weighted Row
 * for the Home grid — pass `Modifier.weight(1f)` to keep them even.
 */
@Composable
fun FlowFinQuickAction(
  icon: ImageVector,
  label: String,
  sub: String,
  tint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(12.dp)

  Column(
    modifier = modifier
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 14.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(7.dp),
  ) {
    FlowFinTileIcon(icon = icon, tint = tint, size = 30.dp)
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.16.em),
      color = palette.text,
    )
    Text(
      text = sub,
      style = FlowFinTheme.typography.body.copy(
        fontSize = 11.sp,
        fontStyle = FontStyle.Italic,
        letterSpacing = (-0.005).em,
      ),
      color = palette.textSoft,
    )
  }
}

@Preview(name = "Quick actions", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewQuickActions() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .padding(24.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    FlowFinQuickAction(
      icon = Icons.Rounded.ArrowUpward,
      label = "Income",
      sub = "received",
      tint = palette.positive,
      onClick = {},
      modifier = Modifier.weight(1f),
    )
    FlowFinQuickAction(
      icon = Icons.Rounded.ArrowDownward,
      label = "Expense",
      sub = "spent",
      tint = palette.negative,
      onClick = {},
      modifier = Modifier.weight(1f),
    )
    FlowFinQuickAction(
      icon = Icons.Rounded.SwapHoriz,
      label = "Move",
      sub = "transfer",
      tint = palette.transfer,
      onClick = {},
      modifier = Modifier.weight(1f),
    )
  }
}
