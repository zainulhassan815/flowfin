package com.flowfin.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * The pinned bottom of every add-form: an optional calculator pad sitting on top of
 * the Save bar, both fixed while the form scrolls underneath.
 *
 * Save is in the same place whether the pad is up or down, which is the whole point —
 * a form's primary action must never depend on scroll position to be reachable.
 *
 * The pad is bound to focus rather than to a per-screen flag: it belongs to the
 * amount, so it rises when the amount has focus and drops when anything else does.
 * A text field's own keyboard takes the same space, so the two are never both up —
 * pass `padVisible = false` whenever the IME is showing.
 *
 * Owns no window insets: it is a [FlowFinScreenScaffold] `bottomBar`, and the
 * scaffold already applies the nav-bar and IME padding for that slot.
 */
@Composable
fun FlowFinFormDock(
  saveLabel: String,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
  padVisible: Boolean = false,
  onHidePad: (() -> Unit)? = null,
  /** Null when the form is ready. Otherwise the one thing still missing, shown under
   *  the disabled label so a blocked Save explains itself instead of sitting inert. */
  blockedReason: String? = null,
  saving: Boolean = false,
  pad: @Composable (() -> Unit)? = null,
) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(palette.bg),
  ) {
    HorizontalDivider(color = palette.border)

    if (pad != null) {
      AnimatedVisibility(
        visible = padVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
      ) {
        Column {
          if (onHidePad != null) PadHandle(onHidePad)
          pad()
        }
      }
    }

    // No window insets here on purpose: FlowFinScreenScaffold already wraps its
    // bottomBar in navigationBarsPadding().imePadding(). Adding them again double-
    // counts the keyboard and squeezes the form off the screen.
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
      FlowFinButton(
        onClick = onSave,
        text = saveLabel,
        modifier = Modifier.fillMaxWidth(),
        enabled = blockedReason == null && !saving,
      )
      if (blockedReason != null) {
        Text(
          text = blockedReason,
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
          style = FlowFinTheme.typography.body,
          color = palette.textSoft,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
private fun PadHandle(onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(palette.surface)
      .clickable(onClick = onClick)
      .padding(vertical = 9.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = FlowFinIcons.ArrowDown,
      contentDescription = null,
      modifier = Modifier.size(13.dp),
      tint = palette.textSoft,
    )
    Text(
      text = "HIDE KEYPAD",
      modifier = Modifier.padding(start = 8.dp),
      style = FlowFinTheme.typography.label.copy(letterSpacing = 0.18.em),
      color = palette.textSoft,
    )
  }
}

@Preview(name = "Form dock — ready", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390)
@Composable
private fun PreviewDockReady() = FlowFinTheme {
  FlowFinFormDock(saveLabel = "Save expense", onSave = {})
}

@Preview(name = "Form dock — blocked", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390)
@Composable
private fun PreviewDockBlocked() = FlowFinTheme {
  FlowFinFormDock(
    saveLabel = "Save expense",
    onSave = {},
    blockedReason = "Pick an account to pay from",
  )
}
