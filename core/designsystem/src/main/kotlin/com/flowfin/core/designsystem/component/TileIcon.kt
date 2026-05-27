package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Rounded-square icon tile used as the leading visual on form rows, account
 * cards, transaction rows, and category pickers.
 *
 * When [tint] is null the tile is neutral (text-muted icon on a transparent
 * surface with a strong border). When [tint] is supplied — typically a
 * category or avatar color — the icon takes the tint and the border and
 * background fade to low-alpha versions of the same hue.
 */
@Composable
fun FlowFinTileIcon(
  icon: ImageVector,
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
  tint: Color? = null,
  size: Dp = 40.dp,
) {
  val palette = FlowFinTheme.colors

  val iconColor = tint ?: palette.textMute
  val borderColor = tint?.copy(alpha = 0.32f) ?: palette.borderStrong
  val backgroundColor = tint?.copy(alpha = 0.08f) ?: Color.Transparent

  // Smaller tiles use a tighter corner radius so they still read as a tile, not a chip.
  val shape = RoundedCornerShape(if (size <= 32.dp) 8.dp else 10.dp)
  val iconSize = size * 0.5f

  Row(
    modifier = modifier
      .size(size)
      .clip(shape)
      .background(backgroundColor)
      .border(width = 1.dp, color = borderColor, shape = shape),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconColor,
      modifier = Modifier.size(iconSize),
    )
  }
}

@Preview(name = "Tile icons", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewTileIcons() = FlowFinTheme {
  Row(
    modifier = Modifier.padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FlowFinTileIcon(icon = Icons.Rounded.AccountBalance, size = 28.dp, tint = FlowFinTheme.colors.categories.bank)
    FlowFinTileIcon(icon = Icons.Rounded.Restaurant, size = 28.dp, tint = FlowFinTheme.colors.categories.food)
    FlowFinTileIcon(icon = Icons.Rounded.ShoppingCart, size = 40.dp, tint = FlowFinTheme.colors.categories.grocery)
    FlowFinTileIcon(icon = Icons.Rounded.AccountBalance, size = 40.dp)
  }
}
