package com.flowfin.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.flowfin.core.designsystem.theme.FlowFinTheme

/** Placeholder for a tab whose screen isn't built yet. */
@Composable
fun ComingSoonScreen(title: String, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(title.uppercase(), style = FlowFinTheme.typography.label, color = FlowFinTheme.colors.textMute)
      Text("Coming soon", style = FlowFinTheme.typography.h2, color = FlowFinTheme.colors.textSoft)
    }
  }
}
