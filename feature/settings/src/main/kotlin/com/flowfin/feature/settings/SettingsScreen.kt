@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.settings

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSettingsCard
import com.flowfin.core.designsystem.component.FlowFinSettingsRow
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.SettingsAccessory
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.ThemePreference
import com.flowfin.core.resources.R

private val HORIZONTAL = 24.dp

@Composable
fun SettingsScreen(
  state: SettingsUiState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onThemeChange: (ThemePreference) -> Unit = {},
  onCategories: () -> Unit = {},
) {
  var themeSheet by remember { mutableStateOf(false) }

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
        text = stringResource(R.string.settings_section_general),
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
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
