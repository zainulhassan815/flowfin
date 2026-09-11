package com.flowfin.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.resources.R

/**
 * What stands in front of the ledger while it's locked.
 *
 * It prompts itself once on appearing, so the common case is a fingerprint and no
 * taps at all. Cancel the prompt and this stays, with the button as the way back
 * — a dead end would be worse than an extra tap.
 */
@Composable
fun LockScreen(activity: FragmentActivity, onUnlocked: () -> Unit) {
  val palette = FlowFinTheme.colors
  val title = stringResource(R.string.lock_prompt_title)
  val subtitle = stringResource(R.string.lock_prompt_subtitle)

  LaunchedEffect(Unit) { promptToUnlock(activity, title, subtitle, onUnlocked) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.settings_wordmark),
      style = FlowFinTheme.typography.h2.copy(fontSize = 20.sp),
      color = palette.textMute,
    )
    Text(
      text = stringResource(R.string.lock_title),
      modifier = Modifier.padding(top = 28.dp),
      style = FlowFinTheme.typography.h1,
      color = palette.text,
    )
    Text(
      text = stringResource(R.string.lock_body),
      modifier = Modifier.padding(top = 8.dp),
      style = FlowFinTheme.typography.body,
      color = palette.textSoft,
      textAlign = TextAlign.Center,
    )
    FlowFinButton(
      onClick = { promptToUnlock(activity, title, subtitle, onUnlocked) },
      text = stringResource(R.string.lock_action),
      modifier = Modifier.padding(top = 32.dp),
    )
  }
}

@Preview(name = "Lock", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewLock() = FlowFinTheme {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(text = "flowfin.", style = FlowFinTheme.typography.h2.copy(fontSize = 20.sp))
    Text(text = "Locked", style = FlowFinTheme.typography.h1)
  }
}
