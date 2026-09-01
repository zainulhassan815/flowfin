package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/** Padding around each cell — also the gap the selection ring sits in. */
val FlowFinKeyGridCellPadding = 5.dp

/** The tile radius the icon cells draw at; a ring around one adds [FlowFinKeyGridCellPadding]. */
val FlowFinKeyGridTileRadius = 10.dp

/**
 * A wrapping grid of selectable string keys — the icon and colour pickers wherever
 * something is tinted and glyphed: a custom category, a budget envelope.
 *
 * [ringShape] is the caller's job because the selection ring has to stay concentric
 * with what it surrounds: a rounded tile's ring takes the tile's radius plus
 * [FlowFinKeyGridCellPadding], a circular swatch's ring is simply a circle.
 */
@Composable
fun FlowFinKeyGrid(
  keys: List<String>,
  selected: String,
  ringShape: Shape,
  ringColor: Color,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
  columns: Int = 6,
  cell: @Composable (key: String, isSelected: Boolean) -> Unit,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    keys.chunked(columns).forEach { rowKeys ->
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        rowKeys.forEach { key ->
          val isSelected = key == selected
          // No cell background: the icon draws its own tile and the swatch is its
          // own shape, so a plate behind either just doubles it. Selection is a
          // tinted ring — the icon's own tint is invisible while the colour is
          // still the neutral default.
          Box(
            modifier = Modifier
              .clip(ringShape)
              .then(if (isSelected) Modifier.border(1.5.dp, ringColor, ringShape) else Modifier)
              .clickable { onSelect(key) }
              .padding(FlowFinKeyGridCellPadding),
            contentAlignment = Alignment.Center,
          ) {
            cell(key, isSelected)
          }
        }
      }
    }
  }
}

@Preview(name = "Key grid", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390)
@Composable
private fun PreviewKeyGrid() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
    FlowFinKeyGrid(
      keys = listOf("a", "b", "c", "d"),
      selected = "b",
      ringShape = RoundedCornerShape(FlowFinKeyGridTileRadius + FlowFinKeyGridCellPadding),
      ringColor = palette.accent,
      onSelect = {},
    ) { _, isSelected ->
      FlowFinTileIcon(
        icon = Icons.Rounded.Restaurant,
        tint = if (isSelected) palette.accent else palette.textSoft,
        size = 40.dp,
      )
    }
    FlowFinKeyGrid(
      keys = listOf("a", "b", "c", "d"),
      selected = "c",
      ringShape = CircleShape,
      ringColor = palette.accent,
      onSelect = {},
    ) { _, _ ->
      Box(Modifier.size(34.dp).clip(CircleShape).background(palette.accent))
    }
  }
}
