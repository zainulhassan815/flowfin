package com.flowfin.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.devtools.DevToolsActivity

/**
 * Debug variant: wraps the app at the root and overlays a hideable "DEV" chip that
 * opens [DevToolsActivity]. All of it — the chip and its show/hide state — lives
 * here in `src/debug`, so the app shell carries no reference to it. The
 * `src/release` twin is a pure passthrough, so neither the chip nor this logic
 * reaches release.
 */
@Composable
fun DevToolsHost(content: @Composable () -> Unit) {
  Box(Modifier.fillMaxSize()) {
    content()
    var collapsed by rememberSaveable { mutableStateOf(false) }
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .safeContentPadding()
        .padding(start = 16.dp, bottom = 88.dp),
    ) {
      if (collapsed) RestoreHandle(onClick = { collapsed = false }) else DevChip(onCollapse = { collapsed = true })
    }
  }
}

@Composable
private fun DevChip(onCollapse: () -> Unit) {
  val context = LocalContext.current
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(percent = 50)
  Row(
    modifier = Modifier
      .clip(shape)
      .background(palette.accent.copy(alpha = 0.12f))
      .border(1.dp, palette.accent.copy(alpha = 0.40f), shape),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "DEV",
      modifier = Modifier
        .clickable { context.startActivity(Intent(context, DevToolsActivity::class.java)) }
        .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.2.em, fontWeight = FontWeight.Bold),
      color = palette.accent,
    )
    Text(
      text = "✕",
      modifier = Modifier
        .clickable(onClick = onCollapse)
        .padding(start = 4.dp, end = 11.dp, top = 7.dp, bottom = 7.dp),
      style = FlowFinTheme.typography.caption.copy(fontSize = 11.sp),
      color = palette.textSoft,
    )
  }
}

/** The minimal handle the chip collapses to — tap to bring the chip back. */
@Composable
private fun RestoreHandle(onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  Box(
    modifier = Modifier
      .size(width = 12.dp, height = 26.dp)
      .clip(RoundedCornerShape(topEnd = 7.dp, bottomEnd = 7.dp))
      .background(palette.accent.copy(alpha = 0.16f))
      .clickable(onClick = onClick),
  )
}
