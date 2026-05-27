package com.flowfin.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

private val TrackWidth = 46.dp
private val TrackHeight = 28.dp
private val KnobSize = 20.dp
private val KnobInset = 3.dp
private val KnobTravel = TrackWidth - KnobSize - (KnobInset * 2)

/**
 * iOS-style pill switch. Off uses a muted surface; on tints cream and slides
 * the knob right. Used for settings and recurring-schedule active/paused.
 *
 * The component is stateless — pass [checked] in and observe [onCheckedChange];
 * the caller owns the truth.
 */
@Composable
fun FlowFinSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(TrackHeight / 2)

  val trackColor by animateColorAsState(
    targetValue = if (checked) palette.accent.copy(alpha = 0.12f) else palette.surface2,
    label = "switch-track",
  )
  val borderColor by animateColorAsState(
    targetValue = if (checked) palette.accent.copy(alpha = 0.40f) else palette.borderStrong,
    label = "switch-border",
  )
  val knobColor by animateColorAsState(
    targetValue = if (checked) palette.accent else palette.textMute,
    label = "switch-knob",
  )
  val knobOffset by animateDpAsState(
    targetValue = if (checked) KnobTravel else 0.dp,
    label = "switch-offset",
  )

  Box(
    modifier = modifier
      .size(width = TrackWidth, height = TrackHeight)
      .toggleable(
        value = checked,
        onValueChange = onCheckedChange,
        enabled = enabled,
        role = Role.Switch,
      )
      .clip(shape)
      .background(trackColor)
      .border(width = 1.dp, color = borderColor, shape = shape),
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(
      modifier = Modifier
        .padding(start = KnobInset)
        .offset(x = knobOffset)
        .size(KnobSize)
        .clip(CircleShape)
        .background(knobColor),
    )
  }
}

@Preview(name = "Switch", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewSwitch() = FlowFinTheme {
  Row(
    modifier = Modifier.padding(32.dp),
    horizontalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    FlowFinSwitch(checked = false, onCheckedChange = {})
    FlowFinSwitch(checked = true, onCheckedChange = {})
    FlowFinSwitch(checked = false, onCheckedChange = {}, enabled = false)
    FlowFinSwitch(checked = true, onCheckedChange = {}, enabled = false)
  }
}
