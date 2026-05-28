package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Circular initial avatar for the people in debts and receivables. An italic
 * serif [initial] on a low-alpha disc of [tint] — pass one of the avatar tints
 * (`FlowFinTheme.colors.avatars.byIndex(n)`) so a person keeps a stable color.
 */
@Composable
fun FlowFinPersonAvatar(
  initial: String,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .size(44.dp)
      .clip(CircleShape)
      .background(tint.copy(alpha = 0.08f))
      .border(1.dp, tint.copy(alpha = 0.28f), CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = initial,
      style = FlowFinTheme.typography.h2.copy(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
          alignment = LineHeightStyle.Alignment.Center,
          trim = LineHeightStyle.Trim.Both,
        ),
      ),
      color = tint,
    )
  }
}

@Preview(name = "Person avatars", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewPersonAvatars() = FlowFinTheme {
  val avatars = FlowFinTheme.colors.avatars
  Row(
    modifier = Modifier.padding(24.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    listOf("A", "H", "S", "M", "I").forEachIndexed { index, letter ->
      FlowFinPersonAvatar(initial = letter, tint = avatars.byIndex(index + 1))
    }
  }
}
