package com.flowfin.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.flowfin.core.designsystem.theme.FlowFinTheme

private val ButtonShape = RoundedCornerShape(14.dp)
private val IconGap = 8.dp
private val IconSize = 14.dp

/**
 * Primary CTA. One per screen — the single most important action.
 *
 * Mono uppercase label on the warm accent fill. Pair with an optional
 * trailing icon (e.g. ArrowForward) for forward-progress actions.
 */
@Composable
fun FlowFinButton(
  onClick: () -> Unit,
  text: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: ImageVector? = null,
  trailingIcon: ImageVector? = null,
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = ButtonShape,
    colors = ButtonDefaults.buttonColors(
      containerColor = FlowFinTheme.colors.accent,
      contentColor = FlowFinTheme.colors.bg,
      disabledContainerColor = FlowFinTheme.colors.surface2,
      disabledContentColor = FlowFinTheme.colors.textFaint,
    ),
    elevation = null,
    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
  ) {
    ButtonRow(
      text = text,
      leadingIcon = leadingIcon,
      trailingIcon = trailingIcon,
      textStyle = FlowFinTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
    )
  }
}

/**
 * Secondary action sitting beside a primary CTA — Cancel, Skip, Back.
 *
 * Outlined in `borderStrong`; same uppercase mono label but a touch lighter
 * tracking. Visually subordinate to the primary button next to it.
 */
@Composable
fun FlowFinOutlinedButton(
  onClick: () -> Unit,
  text: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: ImageVector? = null,
  trailingIcon: ImageVector? = null,
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = ButtonShape,
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = FlowFinTheme.colors.text,
      disabledContentColor = FlowFinTheme.colors.textFaint,
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (enabled) FlowFinTheme.colors.borderStrong else FlowFinTheme.colors.border,
    ),
    contentPadding = PaddingValues(horizontal = 26.dp, vertical = 13.dp),
  ) {
    ButtonRow(
      text = text,
      leadingIcon = leadingIcon,
      trailingIcon = trailingIcon,
      textStyle = FlowFinTheme.typography.label.copy(letterSpacing = 0.18.em),
    )
  }
}

/**
 * Inline text affordance. Carries an accent color so it reads as actionable
 * without weight — used for things like "Save" inside a sheet, "See all"
 * next to a section heading.
 */
@Composable
fun FlowFinTextButton(
  onClick: () -> Unit,
  text: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  TextButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = ButtonShape,
    colors = ButtonDefaults.textButtonColors(
      contentColor = FlowFinTheme.colors.accent,
      disabledContentColor = FlowFinTheme.colors.textFaint,
    ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  ) {
    Text(
      text = text.uppercase(),
      style = FlowFinTheme.typography.label.copy(letterSpacing = 0.18.em),
    )
  }
}

@Composable
private fun ButtonRow(
  text: String,
  leadingIcon: ImageVector?,
  trailingIcon: ImageVector?,
  textStyle: TextStyle,
) {
  if (leadingIcon != null) {
    Icon(
      imageVector = leadingIcon,
      contentDescription = null,
      modifier = Modifier.size(IconSize),
    )
    Spacer(Modifier.width(IconGap))
  }
  Text(text = text.uppercase(), style = textStyle)
  if (trailingIcon != null) {
    Spacer(Modifier.width(IconGap))
    Icon(
      imageVector = trailingIcon,
      contentDescription = null,
      modifier = Modifier.size(IconSize),
    )
  }
}

@Preview(
  name = "Primary",
  backgroundColor = 0xFF08080A,
  showBackground = true,
)
@Composable
private fun PreviewFlowFinButton() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    FlowFinButton(onClick = {}, text = "Save Recurring")
    FlowFinButton(
      onClick = {},
      text = "Continue",
      trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
    )
    FlowFinButton(onClick = {}, text = "Disabled", enabled = false)
  }
}

@Preview(
  name = "Outlined",
  backgroundColor = 0xFF08080A,
  showBackground = true,
)
@Composable
private fun PreviewFlowFinOutlinedButton() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    FlowFinOutlinedButton(onClick = {}, text = "Cancel")
    FlowFinOutlinedButton(
      onClick = {},
      text = "Skip",
      trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
    )
    FlowFinOutlinedButton(onClick = {}, text = "Disabled", enabled = false)
  }
}

@Preview(
  name = "Text",
  backgroundColor = 0xFF08080A,
  showBackground = true,
)
@Composable
private fun PreviewFlowFinTextButton() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    FlowFinTextButton(onClick = {}, text = "Save")
    FlowFinTextButton(onClick = {}, text = "See all")
    FlowFinTextButton(onClick = {}, text = "Disabled", enabled = false)
  }
}
