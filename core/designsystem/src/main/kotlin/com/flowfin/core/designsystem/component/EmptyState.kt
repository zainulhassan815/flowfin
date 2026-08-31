package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * **F — Full empty.** The editorial screen-takeover for a surface with nothing
 * in it yet: a dashed eyebrow, an italic title (with an optional accent
 * [emphasis] phrase), a line of body copy, and the primary [actionLabel] CTA,
 * framed by faint gradient rules. Center it in the empty screen's body.
 */
@Composable
fun FlowFinEmptyState(
  eyebrow: String,
  title: String,
  body: String,
  actionLabel: String,
  onAction: () -> Unit,
  modifier: Modifier = Modifier,
  emphasis: String? = null,
) {
  val palette = FlowFinTheme.colors

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    GradientRule()
    Spacer(Modifier.height(38.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      EyebrowDash()
      Text(
        text = eyebrow.uppercase(),
        style = FlowFinTheme.typography.caption.copy(fontSize = 10.sp, letterSpacing = 0.32.em),
        color = palette.accent,
      )
      EyebrowDash()
    }

    Spacer(Modifier.height(18.dp))
    Text(
      text = emphasized(title, emphasis, palette.accent),
      style = FlowFinTheme.typography.h1.copy(
        fontSize = 32.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.025).em,
        lineHeight = 37.sp,
      ),
      color = palette.text,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(18.dp))
    Text(
      text = body,
      modifier = Modifier.widthIn(max = 300.dp),
      style = FlowFinTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 24.sp),
      color = palette.textMute,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(30.dp))
    FlowFinButton(
      onClick = onAction,
      text = actionLabel,
      trailingIcon = FlowFinIcons.ArrowRight,
      modifier = Modifier.shadow(
        elevation = 16.dp,
        shape = RoundedCornerShape(14.dp),
        spotColor = palette.accent,
        ambientColor = palette.accent,
      ),
    )

    Spacer(Modifier.height(38.dp))
    GradientRule()
  }
}

/**
 * **S — Section empty.** A quiet inline hint for an empty section *inside* a
 * populated screen — a dashed card with a small eyebrow, an italic title, and
 * an optional [actionLabel] link. Never takes over the screen.
 */
@Composable
fun FlowFinSectionEmptyHint(
  eyebrow: String,
  title: String,
  modifier: Modifier = Modifier,
  body: String? = null,
  actionLabel: String? = null,
  onAction: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(12.dp)

  Box(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .drawBehind {
          val stroke = 1.dp.toPx()
          drawRoundRect(
            color = palette.borderStrong,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(
              width = stroke,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            ),
          )
        }
        .padding(horizontal = 18.dp, vertical = 21.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = eyebrow.uppercase(),
        style = FlowFinTheme.typography.caption.copy(fontSize = 8.5.sp, letterSpacing = 0.28.em),
        color = palette.textFaint,
      )
      Text(
        text = title,
        style = FlowFinTheme.typography.h2.copy(fontSize = 16.sp, letterSpacing = (-0.01).em),
        color = palette.text,
        textAlign = TextAlign.Center,
      )
      if (body != null) {
        Text(
          text = body,
          modifier = Modifier.widthIn(max = 240.dp),
          style = FlowFinTheme.typography.body.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
          color = palette.textMute,
          textAlign = TextAlign.Center,
        )
      }
      if (actionLabel != null) {
        Spacer(Modifier.height(2.dp))
        Text(
          text = actionLabel.uppercase(),
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
              drawRoundRect(
                color = palette.accent.copy(alpha = 0.32f),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 1.dp.toPx()),
              )
            }
            .clickable(onClick = onAction)
            .padding(horizontal = 11.dp, vertical = 7.dp),
          style = FlowFinTheme.typography.caption.copy(
            fontSize = 9.sp,
            letterSpacing = 0.2.em,
            fontWeight = FontWeight.SemiBold,
          ),
          color = palette.accent,
        )
      }
    }

    // Accent connector straddling the top edge.
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .width(22.dp)
        .height(1.5.dp)
        .background(
          Brush.horizontalGradient(
            listOf(Color.Transparent, palette.accent, Color.Transparent),
          ),
        ),
    )
  }
}

private fun emphasized(text: String, emphasis: String?, color: Color) =
  buildAnnotatedString {
    val start = emphasis?.let { text.indexOf(it) } ?: -1
    if (emphasis == null || start < 0) {
      append(text)
    } else {
      append(text.substring(0, start))
      withStyle(SpanStyle(color = color, fontStyle = FontStyle.Italic)) { append(emphasis) }
      append(text.substring(start + emphasis.length))
    }
  }

@Composable
private fun GradientRule() {
  Box(
    modifier = Modifier
      .width(80.dp)
      .height(1.dp)
      .background(
        Brush.horizontalGradient(
          listOf(Color.Transparent, FlowFinTheme.colors.borderStrong, Color.Transparent),
        ),
      ),
  )
}

@Composable
private fun EyebrowDash() {
  Box(
    modifier = Modifier
      .width(8.dp)
      .height(1.dp)
      .background(FlowFinTheme.colors.accent.copy(alpha = 0.4f)),
  )
}

@Preview(name = "Empty state · full", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 360)
@Composable
private fun PreviewEmptyStateFull() = FlowFinTheme {
  Box(modifier = Modifier.padding(24.dp)) {
    FlowFinEmptyState(
      eyebrow = "Begin",
      title = "Money lives somewhere. Tell us where.",
      emphasis = "somewhere.",
      body = "Add the first place — your bank, the cash in your wallet, a mobile wallet. Every transaction starts from an account.",
      actionLabel = "Add an account",
      onAction = {},
    )
  }
}

@Preview(name = "Empty state · section", backgroundColor = 0xFF101013, showBackground = true, widthDp = 360)
@Composable
private fun PreviewSectionEmptyHint() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    FlowFinSectionEmptyHint(
      eyebrow = "This week",
      title = "Nothing logged this week.",
      actionLabel = "Add expense",
    )
    FlowFinSectionEmptyHint(
      eyebrow = "Activity",
      title = "Nothing spent yet — that's the start, not an oversight.",
    )
  }
}
