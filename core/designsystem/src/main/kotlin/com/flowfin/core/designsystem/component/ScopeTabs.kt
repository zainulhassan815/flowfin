package com.flowfin.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Underlined scope tabs — the screen-level switch for things like
 * Expense / Income / Transfer or a report's Week / Month / Year range.
 *
 * Tabs sit on a shared baseline; the active one goes italic at full
 * contrast and grows a short accent underline. [indicatorColor] lets the
 * caller tint that underline per option (income reads positive, transfer
 * reads its own blue); return `null` — the default — to fall back to the
 * cream accent. Generic over [T] so callers pass enums or any labelled value.
 */
@Composable
fun <T> FlowFinScopeTabs(
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
  label: @Composable (T) -> String = { it.toString() },
  indicatorColor: (T) -> Color? = { null },
) {
  val palette = FlowFinTheme.colors

  Row(
    modifier = modifier
      .fillMaxWidth()
      .drawBehind {
        val stroke = 1.dp.toPx()
        drawRect(
          color = palette.border,
          topLeft = Offset(0f, size.height - stroke),
          size = Size(size.width, stroke),
        )
      }
      .padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(32.dp),
  ) {
    options.forEach { option ->
      ScopeTab(
        text = label(option),
        active = option == selected,
        indicator = indicatorColor(option) ?: palette.accent,
        onClick = { onSelect(option) },
      )
    }
  }
}

@Composable
private fun ScopeTab(
  text: String,
  active: Boolean,
  indicator: Color,
  onClick: () -> Unit,
) {
  val palette = FlowFinTheme.colors
  val textColor by animateColorAsState(
    targetValue = if (active) palette.text else palette.textSoft,
    label = "scope-text",
  )

  Box(
    modifier = Modifier
      .clickable(onClick = onClick)
      .drawBehind {
        if (active) {
          val height = 2.dp.toPx()
          drawRoundRect(
            color = indicator,
            topLeft = Offset(0f, size.height - height),
            size = Size(size.width, height),
            cornerRadius = CornerRadius(1.dp.toPx()),
          )
        }
      },
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(start = 2.dp, top = 12.dp, end = 2.dp, bottom = 14.dp),
      style = FlowFinTheme.typography.bodyLg.copy(
        fontStyle = if (active) FontStyle.Italic else FontStyle.Normal,
      ),
      color = textColor,
    )
  }
}

@Preview(name = "Scope tabs", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewScopeTabs() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(32.dp),
  ) {
    var scope by remember { mutableStateOf("Expense") }
    FlowFinScopeTabs(
      options = listOf("Expense", "Income", "Transfer"),
      selected = scope,
      onSelect = { scope = it },
      indicatorColor = { option ->
        when (option) {
          "Income" -> palette.positive
          "Transfer" -> palette.transfer
          else -> palette.accent
        }
      },
    )

    var range by remember { mutableStateOf("Month") }
    FlowFinScopeTabs(
      options = listOf("Week", "Month", "Year"),
      selected = range,
      onSelect = { range = it },
    )
  }
}
