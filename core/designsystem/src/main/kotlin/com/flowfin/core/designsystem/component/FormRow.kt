package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Horizontal label-then-value row used wherever the user opens a picker —
 * From, To, Category, Date, and so on. Mono uppercase label on the left,
 * serif value on the right, with an optional aux number and chevron.
 *
 * Pass `value = null` to render [placeholder] in italic muted text — the
 * "not chosen yet" state, suitable for empty forms.
 */
@Composable
fun FlowFinFormRow(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  value: String? = null,
  valueDetail: String? = null,
  placeholder: String = "Tap to set",
  leadingIcon: @Composable (() -> Unit)? = null,
  auxText: String? = null,
  trailingChevron: Boolean = true,
  enabled: Boolean = true,
) {
  val palette = FlowFinTheme.colors

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(enabled = enabled, onClick = onClick)
      .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.22.em),
      color = palette.textSoft,
      modifier = Modifier.width(64.dp),
    )

    Spacer(Modifier.width(14.dp))

    if (leadingIcon != null) {
      leadingIcon()
      Spacer(Modifier.width(10.dp))
    }

    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      if (value != null) {
        Text(
          text = value,
          style = FlowFinTheme.typography.bodyLg,
          color = palette.text,
        )
        if (valueDetail != null) {
          Spacer(Modifier.width(6.dp))
          Text(
            text = valueDetail,
            style = FlowFinTheme.typography.bodyLg,
            color = palette.textMute,
          )
        }
      } else {
        Text(
          text = placeholder,
          style = FlowFinTheme.typography.bodyLg.copy(fontStyle = FontStyle.Italic),
          color = palette.textSoft,
        )
      }
    }

    if (auxText != null) {
      Spacer(Modifier.width(10.dp))
      Text(
        text = auxText,
        style = FlowFinTheme.typography.label.copy(letterSpacing = 0.em),
        color = palette.textMute,
      )
    }

    if (trailingChevron) {
      Spacer(Modifier.width(10.dp))
      Icon(
        imageVector = Icons.Rounded.ChevronRight,
        contentDescription = null,
        tint = palette.textFaint,
        modifier = Modifier.size(14.dp),
      )
    }
  }
}

@Preview(name = "Form rows", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewFormRows() = FlowFinTheme {
  Column(
    modifier = Modifier
      .padding(horizontal = 24.dp, vertical = 16.dp)
      .background(FlowFinTheme.colors.surface),
  ) {
    val palette = FlowFinTheme.colors

    FlowFinFormRow(
      label = "From",
      value = "Bank",
      auxText = "40,000",
      leadingIcon = {
        FlowFinTileIcon(
          icon = Icons.Rounded.AccountBalance,
          tint = palette.categories.bank,
          size = 28.dp,
        )
      },
      onClick = {},
    )
    HorizontalDivider(color = palette.border)
    FlowFinFormRow(
      label = "Category",
      value = "Food & Dining",
      leadingIcon = {
        FlowFinTileIcon(
          icon = Icons.Rounded.Restaurant,
          tint = palette.categories.food,
          size = 28.dp,
        )
      },
      onClick = {},
    )
    HorizontalDivider(color = palette.border)
    FlowFinFormRow(
      label = "Date",
      value = "Today,",
      valueDetail = "25 May 2026",
      onClick = {},
    )
    HorizontalDivider(color = palette.border)
    FlowFinFormRow(
      label = "Note",
      placeholder = "Add a note…",
      trailingChevron = false,
      onClick = {},
    )
  }
}
