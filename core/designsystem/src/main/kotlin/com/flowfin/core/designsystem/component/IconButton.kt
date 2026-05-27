package com.flowfin.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Plain icon-only button — toolbar action, list-row affordance, anywhere a
 * label would be visual noise. 36 dp tappable area; the icon itself is 20 dp.
 */
@Composable
fun FlowFinIconButton(
  onClick: () -> Unit,
  icon: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  IconButton(
    onClick = onClick,
    modifier = modifier.size(36.dp),
    enabled = enabled,
    colors = IconButtonDefaults.iconButtonColors(
      contentColor = FlowFinTheme.colors.textMute,
      disabledContentColor = FlowFinTheme.colors.textFaint,
    ),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      modifier = Modifier.size(20.dp),
    )
  }
}

/**
 * Bordered circular icon button — used when the action sits on its own and
 * needs visual structure (e.g. close on a modal, secondary nav). 38 dp.
 */
@Composable
fun FlowFinOutlinedIconButton(
  onClick: () -> Unit,
  icon: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  OutlinedIconButton(
    onClick = onClick,
    modifier = modifier.size(38.dp),
    enabled = enabled,
    shape = CircleShape,
    colors = IconButtonDefaults.outlinedIconButtonColors(
      contentColor = FlowFinTheme.colors.textMute,
      disabledContentColor = FlowFinTheme.colors.textFaint,
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (enabled) FlowFinTheme.colors.borderStrong else FlowFinTheme.colors.border,
    ),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      modifier = Modifier.size(14.dp),
    )
  }
}

@Preview(name = "Icon buttons", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewIconButtons() = FlowFinTheme {
  Row(
    modifier = Modifier.padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    FlowFinIconButton(onClick = {}, icon = Icons.Rounded.Add, contentDescription = "Add")
    FlowFinIconButton(onClick = {}, icon = Icons.Rounded.Search, contentDescription = "Search")
    FlowFinOutlinedIconButton(onClick = {}, icon = Icons.Rounded.Close, contentDescription = "Close")
    FlowFinOutlinedIconButton(
      onClick = {},
      icon = Icons.Rounded.Close,
      contentDescription = "Close",
      enabled = false,
    )
  }
}
