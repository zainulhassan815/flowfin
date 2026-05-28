package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/** The trailing accessory on a [FlowFinSettingsRow]. */
sealed interface SettingsAccessory {
  /** A mono value with an optional muted tail, e.g. "Rs" + "· PKR". */
  data class Value(val text: String, val detail: String? = null) : SettingsAccessory

  /** A small pill — a count or status. [warn] swaps it to the warning tint. */
  data class Badge(val text: String, val warn: Boolean = false) : SettingsAccessory
}

/**
 * Rounded surface card that groups settings rows. Place rows inside, separated
 * by `HorizontalDivider(color = FlowFinTheme.colors.border)` — the same
 * divider convention the form rows use, which keeps the last row clean.
 */
@Composable
fun FlowFinSettingsCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape),
    content = content,
  )
}

/**
 * A navigable settings row: a serif name over an optional uppercase sub, with
 * an optional trailing [accessory] (a value or a badge) and a chevron. Tapping
 * the row drills in via [onClick].
 */
@Composable
fun FlowFinSettingsRow(
  name: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  sub: String? = null,
  accessory: SettingsAccessory? = null,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    SettingsBody(name = name, sub = sub)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (accessory != null) Accessory(accessory)
      Chevron()
    }
  }
}

/**
 * A settings row whose trailing accessory is a switch. The whole row is
 * tappable; [onCheckedChange] fires from either the row or the switch.
 */
@Composable
fun FlowFinSettingsToggleRow(
  name: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  sub: String? = null,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    SettingsBody(name = name, sub = sub)
    FlowFinSwitch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

/**
 * A destructive action row — dashed negative outline, negative title — for
 * irreversible operations like resetting all data. Stands on its own rather
 * than inside a [FlowFinSettingsCard].
 */
@Composable
fun FlowFinDangerRow(
  icon: ImageVector,
  name: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  sub: String? = null,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(palette.negative.copy(alpha = 0.03f))
      .drawBehind {
        val stroke = 1.dp.toPx()
        drawRoundRect(
          color = palette.negative.copy(alpha = 0.32f),
          topLeft = Offset(stroke / 2, stroke / 2),
          size = Size(size.width - stroke, size.height - stroke),
          cornerRadius = CornerRadius(14.dp.toPx()),
          style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
          ),
        )
      }
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    val iconShape = RoundedCornerShape(10.dp)
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(iconShape)
        .background(palette.negative.copy(alpha = 0.06f))
        .border(1.dp, palette.negative.copy(alpha = 0.28f), iconShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = palette.negative,
        modifier = Modifier.size(20.dp),
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name,
        style = FlowFinTheme.typography.h2.copy(
          fontSize = 16.sp,
          letterSpacing = (-0.01).em,
        ),
        color = palette.negative,
      )
      if (sub != null) {
        Text(
          text = sub.uppercase(),
          modifier = Modifier.padding(top = 3.dp),
          style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.08.em),
          color = palette.textSoft,
        )
      }
    }

    Chevron()
  }
}

@Composable
private fun RowScope.SettingsBody(name: String, sub: String?) {
  val palette = FlowFinTheme.colors
  Column(modifier = Modifier.weight(1f)) {
    Text(
      text = name,
      style = FlowFinTheme.typography.bodyLg.copy(fontSize = 16.sp),
      color = palette.text,
    )
    if (sub != null) {
      Text(
        text = sub.uppercase(),
        modifier = Modifier.padding(top = 3.dp),
        style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.08.em),
        color = palette.textSoft,
      )
    }
  }
}

@Composable
private fun Accessory(accessory: SettingsAccessory) {
  val palette = FlowFinTheme.colors
  when (accessory) {
    is SettingsAccessory.Value -> Text(
      text = buildAnnotatedString {
        append(accessory.text)
        if (accessory.detail != null) {
          append(" ")
          withStyle(SpanStyle(color = palette.textSoft, fontWeight = FontWeight.Normal)) {
            append(accessory.detail)
          }
        }
      },
      style = FlowFinTheme.typography.monoNum.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.01).em,
      ),
      color = palette.text,
    )

    is SettingsAccessory.Badge -> Text(
      text = accessory.text,
      modifier = Modifier
        .clip(RoundedCornerShape(7.dp))
        .background(
          if (accessory.warn) palette.warning.copy(alpha = 0.10f)
          else Color.White.copy(alpha = 0.06f),
        )
        .padding(horizontal = 8.dp, vertical = 3.dp),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.06.em),
      color = if (accessory.warn) palette.warning else palette.text,
    )
  }
}

@Composable
private fun Chevron() {
  Icon(
    imageVector = Icons.Rounded.ChevronRight,
    contentDescription = null,
    tint = FlowFinTheme.colors.textFaint,
    modifier = Modifier.size(14.dp),
  )
}

@Preview(name = "Settings list", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewSettingsList() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  var reminder by remember { mutableStateOf(true) }

  Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    FlowFinSettingsCard {
      FlowFinSettingsRow(
        name = "Currency",
        sub = "Default display symbol",
        accessory = SettingsAccessory.Value("Rs", "· PKR"),
        onClick = {},
      )
      HorizontalDivider(color = palette.border)
      FlowFinSettingsToggleRow(
        name = "Daily reminder",
        sub = "9:00 PM · every evening",
        checked = reminder,
        onCheckedChange = { reminder = it },
      )
      HorizontalDivider(color = palette.border)
      FlowFinSettingsRow(
        name = "Archived accounts",
        sub = "Hidden, history preserved",
        accessory = SettingsAccessory.Badge("2", warn = true),
        onClick = {},
      )
      HorizontalDivider(color = palette.border)
      FlowFinSettingsRow(
        name = "Categories",
        sub = "Income & expense tags",
        accessory = SettingsAccessory.Badge("14 active"),
        onClick = {},
      )
    }

    FlowFinDangerRow(
      icon = Icons.Rounded.Warning,
      name = "Reset all data",
      sub = "Permanent · cannot be undone",
      onClick = {},
    )
  }
}
