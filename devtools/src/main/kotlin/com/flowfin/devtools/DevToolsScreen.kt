@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.devtools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.theme.FlowFinTheme

private val HORIZONTAL = 20.dp

@Composable
internal fun DevToolsScreen(
  busy: Boolean,
  snackbarHostState: SnackbarHostState,
  onClose: () -> Unit,
  onScenario: (DevScenario) -> Unit,
  onWipe: () -> Unit,
  onReseed: () -> Unit,
) {
  Scaffold(
    containerColor = FlowFinTheme.colors.bg,
    topBar = { FlowFinPageHeader(title = "FlowFin Dev", onBack = onClose) },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { innerPadding ->
    Box(Modifier.fillMaxSize().padding(innerPadding)) {
      LazyColumn(contentPadding = PaddingValues(horizontal = HORIZONTAL, vertical = 12.dp)) {
        item { SectionLabel("Scenarios — wipe + seed") }
        items(DevScenario.entries) { scenario ->
          DevCard(
            title = scenario.title,
            blurb = scenario.blurb,
            enabled = !busy,
            onClick = { onScenario(scenario) },
          )
        }
        item { SectionLabel("Primitives") }
        item {
          DevCard("Wipe all data", "Empty every table.", enabled = !busy, tint = FlowFinTheme.colors.negative, onClick = onWipe)
        }
        item {
          DevCard("Reseed default categories", "Re-insert the shipped categories.", enabled = !busy, onClick = onReseed)
        }
      }
      if (busy) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = FlowFinTheme.colors.accent)
        }
      }
    }
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text.uppercase(),
    modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
    style = FlowFinTheme.typography.label,
    color = FlowFinTheme.colors.textMute,
  )
}

@Composable
private fun DevCard(
  title: String,
  blurb: String,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  tint: Color? = null,
  onClick: () -> Unit,
) {
  val palette = FlowFinTheme.colors
  val accent = tint ?: palette.text
  val shape = RoundedCornerShape(12.dp)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp)
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 13.dp),
  ) {
    Text(
      text = title,
      style = FlowFinTheme.typography.bodyLg.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
      color = accent,
    )
    Text(
      text = blurb,
      modifier = Modifier.padding(top = 4.dp),
      style = FlowFinTheme.typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.02.em),
      color = palette.textMute,
    )
  }
}
