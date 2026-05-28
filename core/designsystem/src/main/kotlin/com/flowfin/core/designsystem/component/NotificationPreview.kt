package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * A lockscreen-style notification preview — used to show users what a reminder
 * will look like. A small accent app glyph, the app name and time on one line,
 * and the message body below on a frosted translucent surface.
 *
 * (The real lockscreen blurs the wallpaper behind this; here it's a flat
 * translucent white, which reads the same against the app background.)
 */
@Composable
fun FlowFinNotificationPreview(
  mark: String,
  app: String,
  time: String,
  text: String,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(Color.White.copy(alpha = 0.06f))
      .padding(horizontal = 14.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(RoundedCornerShape(7.dp))
        .background(palette.accent),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = mark,
        style = FlowFinTheme.typography.h2.copy(fontSize = 14.sp, letterSpacing = (-0.04).em),
        color = palette.bg,
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = app.uppercase(),
          modifier = Modifier.alignByBaseline(),
          style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.14.em),
          color = palette.textMute,
        )
        Text(
          text = time,
          modifier = Modifier.alignByBaseline(),
          style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.12.em),
          color = palette.textSoft,
        )
      }
      Spacer(Modifier.height(2.dp))
      Text(
        text = text,
        style = FlowFinTheme.typography.body.copy(fontSize = 13.5.sp, lineHeight = 18.sp),
        color = palette.text,
      )
    }
  }
}

@Preview(name = "Notification preview", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewNotificationPreview() = FlowFinTheme {
  Box(modifier = Modifier.padding(24.dp)) {
    FlowFinNotificationPreview(
      mark = "f·",
      app = "FlowFin",
      time = "9:00 pm",
      text = "Don't forget — log today's spending before bed.",
    )
  }
}
