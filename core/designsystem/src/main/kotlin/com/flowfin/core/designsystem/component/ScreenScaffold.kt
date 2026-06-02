package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * The standard shell for a pushed screen (detail / add / edit). The app shell
 * draws edge-to-edge and consumes no vertical insets, so each screen clears the
 * system bars itself — this scaffold owns that once: the [topBar] clears the
 * status bar, an optional sticky [bottomBar] lifts above the navigation bar and
 * the keyboard, and the body fills the space between.
 *
 * The body decides its own scrolling (a `verticalScroll` Column or a
 * `LazyColumn`). With no [bottomBar], a scrolling body should end with a
 * `Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))` so its
 * last item clears the gesture bar.
 */
@Composable
fun FlowFinScreenScaffold(
  topBar: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  bottomBar: (@Composable () -> Unit)? = null,
  snackbarHost: @Composable () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  Box(modifier.fillMaxSize().background(FlowFinTheme.colors.bg)) {
    Column(Modifier.fillMaxSize()) {
      Box(Modifier.statusBarsPadding()) { topBar() }
      Column(Modifier.fillMaxWidth().weight(1f), content = content)
      if (bottomBar != null) {
        Box(Modifier.navigationBarsPadding().imePadding()) { bottomBar() }
      }
    }
    // Snackbars float at the bottom edge, clearing the nav bar and keyboard.
    Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().imePadding()) {
      snackbarHost()
    }
  }
}

@Preview(name = "Screen scaffold · sticky action", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewScreenScaffold() = FlowFinTheme {
  FlowFinScreenScaffold(
    topBar = { FlowFinPageHeader(title = "New account", onBack = {}) },
    bottomBar = {
      FlowFinButton(
        onClick = {},
        text = "Add account",
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp),
      )
    },
    snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) },
  ) {
    Text(
      text = "Body content",
      modifier = Modifier.padding(24.dp),
      color = FlowFinTheme.colors.text,
    )
  }
}
