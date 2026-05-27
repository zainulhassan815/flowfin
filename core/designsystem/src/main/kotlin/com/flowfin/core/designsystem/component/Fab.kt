package com.flowfin.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * The Add button on Home and a few other launch points. 56 dp, accent fill,
 * accent-tinted shadow so it lifts off the dark surface without competing
 * with content for attention.
 */
@Composable
fun FlowFinFab(
  onClick: () -> Unit,
  icon: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
) {
  FloatingActionButton(
    onClick = onClick,
    modifier = modifier.size(56.dp),
    shape = CircleShape,
    containerColor = FlowFinTheme.colors.accent,
    contentColor = FlowFinTheme.colors.bg,
    elevation = FloatingActionButtonDefaults.elevation(
      defaultElevation = 12.dp,
      pressedElevation = 6.dp,
      focusedElevation = 12.dp,
      hoveredElevation = 12.dp,
    ),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      modifier = Modifier.size(22.dp),
    )
  }
}

@Preview(name = "FAB", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 160, heightDp = 160)
@Composable
private fun PreviewFlowFinFab() = FlowFinTheme {
  Box(modifier = Modifier.padding(32.dp)) {
    FlowFinFab(onClick = {}, icon = Icons.Rounded.Add, contentDescription = "Add")
  }
}
