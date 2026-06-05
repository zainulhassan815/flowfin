package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * A compact row of mono-label chips for a small, single-select filter — the
 * ledger's All / In / Out / Transfers, a report's range, and the like. Chips
 * share the width equally; the active one lifts onto a surface with a stronger
 * border. Generic over [T] so callers pass enums or any labelled value.
 *
 * Reach for [FlowFinSegmentedControl] instead when there are only two or three
 * options with longer, serif labels — its italic pill suits those; these mono
 * chips stay legible when four-plus options must share a phone's width.
 */
@Composable
fun <T> FlowFinFilterChips(
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
  label: (T) -> String = { it.toString() },
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(7.dp),
  ) {
    options.forEach { option ->
      FilterChip(
        text = label(option),
        active = option == selected,
        onClick = { onSelect(option) },
      )
    }
  }
}

@Composable
private fun RowScope.FilterChip(text: String, active: Boolean, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(9.dp)
  Box(
    modifier = Modifier
      .weight(1f)
      .clip(shape)
      .background(if (active) palette.surface else Color.Transparent)
      .border(1.dp, if (active) palette.borderStrong else palette.border, shape)
      .clickable(onClick = onClick)
      .padding(vertical = 9.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text.uppercase(),
      style = FlowFinTheme.typography.caption.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
      color = if (active) palette.text else palette.textSoft,
      maxLines = 1,
      softWrap = false,
    )
  }
}

@Preview(name = "Filter chips", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 360)
@Composable
private fun PreviewFilterChips() = FlowFinTheme {
  var selected by remember { mutableStateOf("All") }
  FlowFinFilterChips(
    options = listOf("All", "In", "Out", "Transfers"),
    selected = selected,
    onSelect = { selected = it },
    modifier = Modifier.padding(20.dp),
    label = { it },
  )
}
