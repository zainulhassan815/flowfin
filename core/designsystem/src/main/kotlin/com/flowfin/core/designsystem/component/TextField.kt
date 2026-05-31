package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Underline-style single-line text input. Used for name fields and other
 * lightweight prose entry where the surrounding surface already provides
 * enough containment. The cursor takes the cream accent so it reads as the
 * active focal point on the dark surface.
 *
 * Pass [label] for a mono caption above the field, [errorMessage] to mark it invalid
 * (the underline and cursor turn negative and the message shows beneath), and [prefix]
 * for a leading slot on the input line — e.g. a currency symbol like "Rs".
 */
@Composable
fun FlowFinTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  placeholder: String? = null,
  errorMessage: String? = null,
  prefix: (@Composable () -> Unit)? = null,
  enabled: Boolean = true,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
  val palette = FlowFinTheme.colors
  val isError = errorMessage != null
  val textStyle = FlowFinTheme.typography.bodyLg.copy(
    color = palette.text,
    fontSize = 20.sp,
  )

  Column(modifier = modifier.fillMaxWidth()) {
    if (label != null) {
      Text(
        text = label.uppercase(),
        modifier = Modifier.padding(bottom = 4.dp),
        style = FlowFinTheme.typography.label,
        color = palette.textSoft,
      )
    }

    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 12.dp),
      enabled = enabled,
      singleLine = true,
      textStyle = textStyle,
      cursorBrush = SolidColor(if (isError) palette.negative else palette.accent),
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      decorationBox = { inner ->
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (prefix != null) {
              prefix()
              Spacer(Modifier.width(8.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
              if (value.isEmpty() && placeholder != null) {
                Text(
                  text = placeholder,
                  style = textStyle.copy(color = palette.textSoft),
                )
              }
              inner()
            }
          }
          Spacer(Modifier.size(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .size(width = 0.dp, height = 1.dp)
              .background(if (isError) palette.negative else palette.borderStrong),
          )
        }
      },
    )

    if (errorMessage != null) {
      Text(
        text = errorMessage,
        modifier = Modifier.padding(top = 8.dp),
        style = FlowFinTheme.typography.caption,
        color = palette.negative,
      )
    }
  }
}

/**
 * Search-style text input. Rounded surface tile with a leading search icon
 * and an italic placeholder — picker sheets and screen-level search bars.
 */
@Composable
fun FlowFinSearchField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Search…",
  enabled: Boolean = true,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(12.dp)
  val textStyle = FlowFinTheme.typography.body.copy(color = palette.text)

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(palette.surface)
      .border(width = 1.dp, color = palette.border, shape = shape),
    enabled = enabled,
    singleLine = true,
    textStyle = textStyle,
    cursorBrush = SolidColor(palette.accent),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = keyboardActions,
    decorationBox = { inner ->
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Rounded.Search,
          contentDescription = null,
          tint = palette.textSoft,
          modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
          if (value.isEmpty()) {
            Text(
              text = placeholder,
              style = textStyle.copy(
                color = palette.textSoft,
                fontStyle = FontStyle.Italic,
              ),
            )
          }
          inner()
        }
      }
    },
  )
}

@Preview(name = "Text inputs", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewTextInputs() = FlowFinTheme {
  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    var name by remember { mutableStateOf("Gym Membership") }
    FlowFinTextField(value = name, onValueChange = { name = it }, placeholder = "Name")

    var empty by remember { mutableStateOf("") }
    FlowFinTextField(value = empty, onValueChange = { empty = it }, placeholder = "Add a note…")

    var labelled by remember { mutableStateOf("HBL Savings") }
    FlowFinTextField(
      value = labelled,
      onValueChange = { labelled = it },
      label = "Account name",
      placeholder = "e.g. HBL Savings",
    )

    var taken by remember { mutableStateOf("Cash") }
    FlowFinTextField(
      value = taken,
      onValueChange = { taken = it },
      label = "Account name",
      errorMessage = "An account called “Cash” already exists",
    )

    var amount by remember { mutableStateOf("40,000") }
    FlowFinTextField(
      value = amount,
      onValueChange = { amount = it },
      label = "Opening balance",
      placeholder = "0.00",
      prefix = {
        Text(
          text = "Rs",
          style = FlowFinTheme.typography.h2.copy(fontSize = 18.sp),
          color = FlowFinTheme.colors.textMute,
        )
      },
    )

    var query by remember { mutableStateOf("") }
    FlowFinSearchField(value = query, onValueChange = { query = it }, placeholder = "Search categories…")

    var withText by remember { mutableStateOf("food") }
    FlowFinSearchField(value = withText, onValueChange = { withText = it })
  }
}
