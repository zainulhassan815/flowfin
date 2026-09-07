@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSettingsCard
import com.flowfin.core.designsystem.component.FlowFinSettingsRow
import com.flowfin.core.designsystem.component.FlowFinSettingsToggleRow
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.SettingsAccessory
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.ThemePreference
import com.flowfin.core.resources.R
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val HORIZONTAL = 24.dp

@Composable
fun SettingsScreen(
  state: SettingsUiState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onThemeChange: (ThemePreference) -> Unit = {},
  onDailyReminderChange: (Boolean) -> Unit = {},
  onReminderTimeChange: (LocalTime) -> Unit = {},
  onPaymentAlertsChange: (Boolean) -> Unit = {},
  onBudgetAlertsChange: (Boolean) -> Unit = {},
  onCategories: () -> Unit = {},
) {
  var themeSheet by remember { mutableStateOf(false) }
  var timeSheet by remember { mutableStateOf(false) }

  // Asked for in context, when a switch is turned on — never on cold start. The
  // answer is re-read after the dialog rather than assumed, so a decline shows
  // the user why their switches aren't doing anything.
  val context = LocalContext.current
  var allowed by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
  val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
    allowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
  }
  fun enable(on: Boolean, apply: (Boolean) -> Unit) {
    apply(on)
    if (on && !allowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = HORIZONTAL),
    ) {
      SectionLabel(
        text = stringResource(R.string.settings_section_notifications),
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
      )
      FlowFinSettingsCard {
        FlowFinSettingsToggleRow(
          name = stringResource(R.string.settings_reminder),
          sub = stringResource(R.string.settings_reminder_sub, state.dailyReminderTime.label()),
          checked = state.dailyReminderEnabled,
          onCheckedChange = { enable(it, onDailyReminderChange) },
        )
        HorizontalDivider(color = FlowFinTheme.colors.border)
        FlowFinSettingsRow(
          name = stringResource(R.string.settings_reminder_time),
          sub = stringResource(R.string.settings_reminder_time_sub),
          accessory = SettingsAccessory.Value(state.dailyReminderTime.label()),
          onClick = { timeSheet = true },
        )
        HorizontalDivider(color = FlowFinTheme.colors.border)
        FlowFinSettingsToggleRow(
          name = stringResource(R.string.settings_payment_alerts),
          sub = stringResource(R.string.settings_payment_alerts_sub),
          checked = state.paymentAlertsEnabled,
          onCheckedChange = { enable(it, onPaymentAlertsChange) },
        )
        HorizontalDivider(color = FlowFinTheme.colors.border)
        FlowFinSettingsToggleRow(
          name = stringResource(R.string.settings_budget_alerts),
          sub = stringResource(R.string.settings_budget_alerts_sub),
          checked = state.budgetAlertsEnabled,
          onCheckedChange = { enable(it, onBudgetAlertsChange) },
        )
      }
      if (!allowed) {
        // The switches default on, so a fresh install never flips one and never
        // gets asked. This notice is the ask — and it goes to Android's own
        // settings, which works whether the permission is unasked, denied once,
        // or denied for good.
        Text(
          text = stringResource(R.string.settings_notifications_blocked),
          modifier = Modifier
            .clickable { context.startActivity(appNotificationSettings(context)) }
            .padding(top = 8.dp, start = 4.dp, end = 4.dp),
          style = FlowFinTheme.typography.caption,
          color = FlowFinTheme.colors.warning,
        )
      }

      SectionLabel(
        text = stringResource(R.string.settings_section_general),
        modifier = Modifier.padding(top = 26.dp, bottom = 10.dp),
      )
      FlowFinSettingsCard {
        FlowFinSettingsRow(
          name = stringResource(R.string.settings_appearance),
          sub = stringResource(R.string.settings_appearance_sub),
          accessory = SettingsAccessory.Value(stringResource(state.theme.labelRes())),
          onClick = { themeSheet = true },
        )
      }

      SectionLabel(
        text = stringResource(R.string.settings_section_organize),
        modifier = Modifier.padding(top = 26.dp, bottom = 10.dp),
      )
      FlowFinSettingsCard {
        FlowFinSettingsRow(
          name = stringResource(R.string.settings_categories),
          sub = stringResource(R.string.settings_categories_sub),
          accessory = SettingsAccessory.Badge(
            stringResource(R.string.settings_categories_badge, state.activeCategoryCount),
          ),
          onClick = onCategories,
        )
      }

      Spacer(Modifier.padding(top = 36.dp))
      Footer(state)
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
  }

  if (timeSheet) {
    TimeSheet(
      selected = state.dailyReminderTime,
      onDismiss = { timeSheet = false },
      onSelect = {
        timeSheet = false
        onReminderTimeChange(it)
      },
    )
  }

  if (themeSheet) {
    ThemeSheet(
      selected = state.theme,
      onDismiss = { themeSheet = false },
      onSelect = {
        themeSheet = false
        onThemeChange(it)
      },
    )
  }
}

/** Android's own notification settings for FlowFin — the one destination that is
 *  correct in every permission state. */
private fun appNotificationSettings(context: android.content.Context) =
  android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)

/** "8:00 PM" or "20:00", whichever the device's locale uses. */
private fun LocalTime.label(): String =
  DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(toJavaLocalTime())

/** M3's time *input* in a FlowFin sheet, not its dial: two fields and a period
 *  toggle fit above the fold, and typing 8:00 beats dragging a hand to it in an
 *  app whose forms are already keypad-first. The 12/24-hour handling is M3's. */
@Composable
private fun TimeSheet(
  selected: LocalTime,
  onDismiss: () -> Unit,
  onSelect: (LocalTime) -> Unit,
) {
  val palette = FlowFinTheme.colors
  val picker = rememberTimePickerState(initialHour = selected.hour, initialMinute = selected.minute)
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.settings_time_sheet_title), onClose = onDismiss)
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL).padding(bottom = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      TimeInput(
        state = picker,
        colors = TimePickerDefaults.colors(
          timeSelectorSelectedContainerColor = palette.accent.copy(alpha = 0.12f),
          timeSelectorSelectedContentColor = palette.text,
          timeSelectorUnselectedContainerColor = palette.surface2,
          timeSelectorUnselectedContentColor = palette.textMute,
          periodSelectorSelectedContainerColor = palette.accent.copy(alpha = 0.12f),
          periodSelectorSelectedContentColor = palette.text,
          periodSelectorUnselectedContentColor = palette.textMute,
          periodSelectorBorderColor = palette.borderStrong,
        ),
      )
      Spacer(Modifier.padding(top = 8.dp))
      FlowFinButton(
        onClick = { onSelect(LocalTime(picker.hour, picker.minute)) },
        text = stringResource(R.string.action_done),
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

private fun ThemePreference.labelRes(): Int = when (this) {
  ThemePreference.LIGHT -> R.string.settings_theme_light
  ThemePreference.DARK -> R.string.settings_theme_dark
  ThemePreference.SYSTEM -> R.string.settings_theme_system
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text.uppercase(),
    modifier = modifier,
    style = FlowFinTheme.typography.label,
    color = FlowFinTheme.colors.textSoft,
  )
}

@Composable
private fun ThemeSheet(
  selected: ThemePreference,
  onDismiss: () -> Unit,
  onSelect: (ThemePreference) -> Unit,
) {
  val palette = FlowFinTheme.colors
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.settings_appearance), onClose = onDismiss)
    Column(Modifier.padding(horizontal = HORIZONTAL).padding(bottom = 12.dp)) {
      ThemePreference.entries.forEach { option ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(option) }
            .padding(vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(Modifier.weight(1f)) {
            Text(
              text = stringResource(option.labelRes()),
              style = FlowFinTheme.typography.bodyLg,
              color = palette.text,
            )
            Text(
              text = stringResource(option.subRes()),
              modifier = Modifier.padding(top = 2.dp),
              style = FlowFinTheme.typography.caption,
              color = palette.textSoft,
            )
          }
          if (option == selected) {
            Icon(
              imageVector = FlowFinIcons.Check,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = palette.text,
            )
          }
        }
      }
    }
  }
}

private fun ThemePreference.subRes(): Int = when (this) {
  ThemePreference.LIGHT -> R.string.settings_theme_light_sub
  ThemePreference.DARK -> R.string.settings_theme_dark_sub
  ThemePreference.SYSTEM -> R.string.settings_theme_system_sub
}

@Composable
private fun Footer(state: SettingsUiState) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = stringResource(R.string.settings_wordmark),
      style = FlowFinTheme.typography.h2.copy(fontSize = 17.sp),
      color = palette.textMute,
    )
    Text(
      text = stringResource(R.string.settings_version, state.versionName, state.versionCode),
      style = FlowFinTheme.typography.caption,
      color = palette.textFaint,
      textAlign = TextAlign.Center,
    )
  }
}

@Preview(name = "Settings", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewSettings() = FlowFinTheme {
  SettingsScreen(state = SettingsUiState(versionName = "1.0.0", versionCode = "27"))
}
