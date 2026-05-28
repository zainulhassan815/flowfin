package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * The app's bottom navigation bar — a full-width strip with a hairline top
 * border on a near-opaque background. Fill it with [FlowFinNavBarItem]s; each
 * takes an even share of the width.
 *
 * (The real lockscreen-style blur behind it isn't reproducible in Compose, so
 * the background is a flat 92%-opaque bg, which reads the same in practice.)
 */
@Composable
fun FlowFinNavBar(
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(88.dp)
      .background(palette.bg.copy(alpha = 0.92f))
      .drawBehind {
        drawRect(
          color = palette.border,
          topLeft = Offset(0f, 0f),
          size = Size(size.width, 1.dp.toPx()),
        )
      }
      .padding(start = 12.dp, top = 14.dp, end = 12.dp),
    verticalAlignment = Alignment.Top,
    content = content,
  )
}

/**
 * One tab in the [FlowFinNavBar]: a stacked icon and uppercase mono label. The
 * [selected] tab reads at full contrast; the rest stay muted. Selection is the
 * only feedback — there's no ripple — matching the design.
 */
@Composable
fun RowScope.FlowFinNavBarItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  val color = if (selected) palette.text else palette.textMute

  Column(
    modifier = modifier
      .weight(1f)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
      )
      .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = color,
      modifier = Modifier.size(20.dp),
    )
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.caption.copy(fontSize = 9.5.sp, letterSpacing = 0.16.em),
      color = color,
    )
    Box(
      modifier = Modifier
        .size(width = 18.dp, height = 2.5.dp)
        .clip(CircleShape)
        .background(if (selected) palette.accent else Color.Transparent),
    )
  }
}

@Preview(name = "Navigation", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 420)
@Composable
private fun PreviewNavBar() = FlowFinTheme {
  var selected by remember { mutableStateOf(1) }
  FlowFinNavBar {
    FlowFinNavBarItem(Icons.Rounded.Home, "Home", selected == 0, onClick = { selected = 0 })
    FlowFinNavBarItem(Icons.Rounded.AccountBalanceWallet, "Accounts", selected == 1, onClick = { selected = 1 })
    FlowFinNavBarItem(Icons.Rounded.Autorenew, "Recur", selected == 2, onClick = { selected = 2 })
    FlowFinNavBarItem(Icons.Rounded.People, "Debts", selected == 3, onClick = { selected = 3 })
    FlowFinNavBarItem(Icons.Rounded.BarChart, "Reports", selected == 4, onClick = { selected = 4 })
  }
}
