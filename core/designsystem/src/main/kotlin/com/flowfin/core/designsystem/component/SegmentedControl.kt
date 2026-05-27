package com.flowfin.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * iOS-style pill segmented control for small enumerations — recurring
 * cadence (Weekly / Monthly / Yearly), filter scope, etc.
 *
 * The active item is italicized and lifts on an accent-tinted surface with
 * an inset accent border; inactive items stay quiet in muted serif. Generic
 * over [T] so the caller passes enums or any value with a sensible label.
 */
@Composable
fun <T> FlowFinSegmentedControl(
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
  label: (T) -> String = { it.toString() },
) {
  val palette = FlowFinTheme.colors

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(palette.surface)
      .border(1.dp, palette.border, RoundedCornerShape(12.dp))
      .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    options.forEach { option ->
      SegmentedItem(
        text = label(option),
        active = option == selected,
        onClick = { onSelect(option) },
      )
    }
  }
}

@Composable
private fun RowScope.SegmentedItem(
  text: String,
  active: Boolean,
  onClick: () -> Unit,
) {
  val palette = FlowFinTheme.colors

  val background by animateColorAsState(
    targetValue = if (active) palette.accent.copy(alpha = 0.10f) else Color.Transparent,
    label = "seg-bg",
  )
  val borderColor by animateColorAsState(
    targetValue = if (active) palette.accent.copy(alpha = 0.18f) else Color.Transparent,
    label = "seg-border",
  )
  val textColor by animateColorAsState(
    targetValue = if (active) palette.text else palette.textSoft,
    label = "seg-text",
  )

  val shape = RoundedCornerShape(9.dp)

  Box(
    modifier = Modifier
      .weight(1f)
      .clip(shape)
      .background(background)
      .border(1.dp, borderColor, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = FlowFinTheme.typography.body.copy(
        fontStyle = if (active) FontStyle.Italic else FontStyle.Normal,
      ),
      color = textColor,
      textAlign = TextAlign.Center,
    )
  }
}

@Preview(name = "Segmented control", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewSegmentedControl() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    var cadence by remember { mutableStateOf("Monthly") }
    FlowFinSegmentedControl(
      options = listOf("Weekly", "Monthly", "Yearly"),
      selected = cadence,
      onSelect = { cadence = it },
    )

    var scope by remember { mutableStateOf("Expense") }
    FlowFinSegmentedControl(
      options = listOf("Expense", "Income", "Transfer"),
      selected = scope,
      onSelect = { scope = it },
    )
  }
}
