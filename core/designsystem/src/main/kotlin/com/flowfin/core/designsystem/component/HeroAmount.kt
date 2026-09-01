package com.flowfin.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * The big amount readout on Home and the add/record screens. An italic-serif
 * currency mark sits beside a large Geist Mono figure, with a quieter decimal
 * tail. The caller supplies the already-formatted pieces — [whole] (with its
 * thousands separators), the optional [decimal] tail (including its separator,
 * e.g. ".00"), and the [currency] mark — so this component never guesses a
 * locale.
 *
 * When [expression] is set the live calculator line shows above the figure:
 * operands read muted, operator glyphs pick up [tint], and a blinking caret
 * trails the end. [tint] also lets the figure echo the active transaction type.
 */
@Composable
fun FlowFinHeroAmount(
  whole: String,
  modifier: Modifier = Modifier,
  currency: String = "Rs",
  decimal: String? = null,
  expression: String? = null,
  tint: Color = FlowFinTheme.colors.accent,
) {
  Column(
    modifier = modifier.padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    if (expression != null) {
      ExpressionLine(expression = expression, tint = tint)
    }
    ResultLine(currency = currency, whole = whole, decimal = decimal)
  }
}

@Composable
private fun ResultLine(currency: String, whole: String, decimal: String?) {
  val palette = FlowFinTheme.colors
  Row {
    Text(
      text = currency,
      modifier = Modifier.alignByBaseline(),
      style = FlowFinTheme.typography.h2.copy(fontSize = 24.sp),
      color = palette.textMute,
    )
    Text(
      text = whole,
      modifier = Modifier
        .alignByBaseline()
        .padding(start = 8.dp),
      style = FlowFinTheme.typography.hero.copy(
        fontSize = 52.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Normal,
      ),
      color = palette.text,
    )
    if (decimal != null) {
      Text(
        text = decimal,
        modifier = Modifier
          .alignByBaseline()
          .padding(start = 4.dp),
        style = FlowFinTheme.typography.monoNum.copy(fontWeight = FontWeight.Normal),
        color = palette.textMute,
      )
    }
  }
}

@Composable
private fun ExpressionLine(expression: String, tint: Color) {
  val palette = FlowFinTheme.colors
  val operators = remember {
    CalculatorOperator.entries.mapNotNull { it.symbol.singleOrNull() }.toSet()
  }
  val text = buildAnnotatedString {
    expression.forEach { char ->
      if (char in operators) {
        withStyle(SpanStyle(color = tint)) { append(char) }
      } else {
        append(char)
      }
    }
  }

  Row(
    horizontalArrangement = Arrangement.spacedBy(1.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = text,
      style = FlowFinTheme.typography.monoNum.copy(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.01).em,
      ),
      color = palette.textMute,
    )
    Caret(tint = tint)
  }
}

@Composable
private fun Caret(tint: Color) {
  val transition = rememberInfiniteTransition(label = "caret")
  val alpha by transition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 1000
        1f at 0
        1f at 499
        0f at 500
        0f at 999
      },
      repeatMode = RepeatMode.Restart,
    ),
    label = "caret-alpha",
  )

  Box(
    modifier = Modifier
      .alpha(alpha)
      .width(2.dp)
      .height(14.dp)
      .clip(RoundedCornerShape(1.dp))
      .background(tint),
  )
}

@Preview(name = "Hero amount", backgroundColor = 0xFF101013, showBackground = true)
@Composable
private fun PreviewHeroAmount() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    FlowFinHeroAmount(whole = "12,400", decimal = ".00")

    FlowFinHeroAmount(
      whole = "600",
      decimal = ".00",
      expression = "1,800 ÷ 3",
    )

    FlowFinHeroAmount(
      whole = "2,500",
      decimal = ".00",
      tint = palette.positive,
    )
  }
}

/**
 * The amount as a *field*: the hero, plus a kicker label above and a rule beneath
 * that goes accent when it has focus — the same cue a text field uses, so nothing
 * has to tell the user it's tappable.
 *
 * Focus is what raises the keypad (see [FlowFinFormDock]), so the whole block is
 * the tap target rather than the digits alone.
 */
@Composable
fun FlowFinAmountField(
  whole: String,
  label: String,
  focused: Boolean,
  onFocus: () -> Unit,
  modifier: Modifier = Modifier,
  currency: String = "Rs",
  decimal: String? = null,
  expression: String? = null,
  tint: Color = FlowFinTheme.colors.accent,
  empty: Boolean = false,
) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onFocus)
      .padding(top = 10.dp, bottom = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.label,
      color = palette.textSoft,
    )
    FlowFinHeroAmount(
      whole = whole,
      currency = currency,
      decimal = decimal,
      expression = expression,
      tint = if (empty) palette.textFaint else tint,
    )
    Box(
      Modifier
        .padding(top = 2.dp)
        .width(168.dp)
        .height(if (focused) 1.5.dp else 1.dp)
        .background(if (focused) tint else palette.border),
    )
  }
}
