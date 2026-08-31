package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

private val KeyGap = 7.dp
private val KeyHeight = 46.dp

/** The arithmetic operators the pad can emit. [symbol] is the glyph it shows. */
enum class CalculatorOperator(val symbol: String) {
  Plus("+"),
  Minus("−"),
  Times("×"),
  Divide("÷"),
  Percent("%"),
}

/** A single press from [FlowFinCalculatorPad]. The pad is stateless — the
 *  caller folds these into an expression and amount. */
sealed interface CalculatorKey {
  data class Digit(val value: Int) : CalculatorKey
  data class Operator(val type: CalculatorOperator) : CalculatorKey
  data object Decimal : CalculatorKey
  data object Clear : CalculatorKey
  data object Backspace : CalculatorKey
  data object Equals : CalculatorKey
}

/**
 * Numeric keypad for amount entry. A 4-column grid of mono keys: digits sit
 * quiet on the raised surface, operators read in [tint], and equals is the
 * filled primary. The pad holds no state — every press is reported through
 * [onKey] and the screen owns the running expression.
 *
 * [tint] colors the operator and equals keys so the pad can echo the active
 * transaction type (expense reads cream accent, income positive, etc.).
 *
 * Size the pad through [modifier] (e.g. `Modifier.width(320.dp)` or
 * `fillMaxWidth()`); the keys flex to fill the given width.
 */
@Composable
fun FlowFinCalculatorPad(
  onKey: (CalculatorKey) -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = FlowFinTheme.colors.accent,
) {
  val palette = FlowFinTheme.colors

  KeyGrid(
    columns = 4,
    gap = KeyGap,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(palette.surface)
      .border(1.dp, palette.border, RoundedCornerShape(14.dp))
      .padding(14.dp),
  ) {
    FunctionKey("C") { onKey(CalculatorKey.Clear) }
    BackspaceKey { onKey(CalculatorKey.Backspace) }
    OperatorKey(CalculatorOperator.Percent, tint, onKey)
    OperatorKey(CalculatorOperator.Divide, tint, onKey)

    DigitKey(7, onKey)
    DigitKey(8, onKey)
    DigitKey(9, onKey)
    OperatorKey(CalculatorOperator.Times, tint, onKey)

    DigitKey(4, onKey)
    DigitKey(5, onKey)
    DigitKey(6, onKey)
    OperatorKey(CalculatorOperator.Minus, tint, onKey)

    DigitKey(1, onKey)
    DigitKey(2, onKey)
    DigitKey(3, onKey)
    OperatorKey(CalculatorOperator.Plus, tint, onKey)

    DigitKey(0, onKey, span = 2)
    DigitKey(".", fontSize = 24.sp) { onKey(CalculatorKey.Decimal) }
    EqualsKey(tint) { onKey(CalculatorKey.Equals) }
  }
}

/**
 * Fixed (non-scrolling) grid: children flow left-to-right into [columns] of
 * equal width separated by [gap], wrapping to a new row when full. A child may
 * claim several columns via [Modifier.span]; the span covers the interior gaps
 * too, so spanning keys stay flush with the columns above. Column edges are
 * computed by shared boundaries, so widths tile the full measure with no
 * rounding gap on the trailing edge.
 */
@Composable
private fun KeyGrid(
  columns: Int,
  gap: Dp,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Layout(content = content, modifier = modifier) { measurables, constraints ->
    val gapPx = gap.roundToPx()
    val width = constraints.maxWidth
    val cellsWidth = width - gapPx * (columns - 1)

    fun edge(column: Int) = column * gapPx + column * cellsWidth / columns

    class Cell(val placeable: Placeable, val x: Int, val row: Int)

    val cells = ArrayList<Cell>(measurables.size)
    var column = 0
    var row = 0
    var rowHeight = 0
    measurables.forEach { measurable ->
      val span = (measurable.parentData as? KeySpan)?.span ?: 1
      if (column + span > columns) {
        column = 0
        row++
      }
      val left = edge(column)
      val right = edge(column + span) - gapPx
      val placeable = measurable.measure(Constraints.fixedWidth(right - left))
      rowHeight = maxOf(rowHeight, placeable.height)
      cells += Cell(placeable, left, row)
      column += span
    }

    val rowCount = row + 1
    val height = rowCount * rowHeight + (rowCount - 1) * gapPx
    layout(width, height) {
      cells.forEach { it.placeable.place(it.x, it.row * (rowHeight + gapPx)) }
    }
  }
}

private data class KeySpan(val span: Int) : ParentDataModifier {
  override fun Density.modifyParentData(parentData: Any?): Any = this@KeySpan
}

private fun Modifier.span(columns: Int): Modifier =
  if (columns <= 1) this else this.then(KeySpan(columns))

private enum class KeyTone { Digit, Function, Operator, Equals }

@Composable
private fun CalcKey(
  tone: KeyTone,
  onClick: () -> Unit,
  span: Int = 1,
  tint: Color = Color.Unspecified,
  content: @Composable () -> Unit,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(12.dp)
  val background = when (tone) {
    KeyTone.Digit, KeyTone.Function -> palette.surface2
    KeyTone.Operator -> tint.copy(alpha = 0.04f)
    KeyTone.Equals -> tint
  }
  val borderColor = when (tone) {
    KeyTone.Digit, KeyTone.Function -> palette.border
    KeyTone.Operator -> tint.copy(alpha = 0.16f)
    KeyTone.Equals -> tint
  }

  Box(
    modifier = Modifier
      .span(span)
      .height(KeyHeight)
      .clip(shape)
      .background(background)
      .border(1.dp, borderColor, shape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}

@Composable
private fun DigitKey(
  digit: Int,
  onKey: (CalculatorKey) -> Unit,
  span: Int = 1,
) = DigitKey(digit.toString(), span) { onKey(CalculatorKey.Digit(digit)) }

@Composable
private fun DigitKey(
  label: String,
  span: Int = 1,
  fontSize: TextUnit = 20.sp,
  onClick: () -> Unit,
) {
  CalcKey(KeyTone.Digit, onClick, span = span) {
    Text(
      text = label,
      style = FlowFinTheme.typography.monoNum.copy(
        fontSize = fontSize,
        fontWeight = FontWeight.Normal,
      ),
      color = FlowFinTheme.colors.text,
    )
  }
}

@Composable
private fun FunctionKey(label: String, onClick: () -> Unit) {
  CalcKey(KeyTone.Function, onClick) {
    Text(
      text = label,
      style = FlowFinTheme.typography.monoNum.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.05.em,
      ),
      color = FlowFinTheme.colors.textMute,
    )
  }
}

@Composable
private fun BackspaceKey(onClick: () -> Unit) {
  CalcKey(KeyTone.Function, onClick) {
    Icon(
      imageVector = Icons.AutoMirrored.Rounded.Backspace,
      contentDescription = "Backspace",
      tint = FlowFinTheme.colors.textMute,
      modifier = Modifier.size(18.dp),
    )
  }
}

@Composable
private fun OperatorKey(
  operator: CalculatorOperator,
  tint: Color,
  onKey: (CalculatorKey) -> Unit,
) {
  CalcKey(KeyTone.Operator, onClick = { onKey(CalculatorKey.Operator(operator)) }, tint = tint) {
    Text(
      text = operator.symbol,
      style = FlowFinTheme.typography.monoNum,
      color = tint,
    )
  }
}

@Composable
private fun EqualsKey(tint: Color, onClick: () -> Unit) {
  CalcKey(KeyTone.Equals, onClick, tint = tint) {
    Text(
      text = "=",
      style = FlowFinTheme.typography.monoNum.copy(fontWeight = FontWeight.SemiBold),
      color = FlowFinTheme.colors.onAccent,
    )
  }
}

@Preview(name = "Calculator pad", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewCalculatorPad() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    FlowFinCalculatorPad(onKey = {}, modifier = Modifier.width(320.dp))
    FlowFinCalculatorPad(
      onKey = {},
      modifier = Modifier.width(320.dp),
      tint = palette.positive,
    )
  }
}
