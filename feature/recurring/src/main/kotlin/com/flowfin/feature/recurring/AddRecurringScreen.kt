@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinCalculatorPad
import com.flowfin.core.designsystem.component.FlowFinAmountField
import com.flowfin.core.designsystem.component.FlowFinFormDock
import com.flowfin.core.designsystem.component.FlowFinFormRow
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinPickerRow
import com.flowfin.core.designsystem.component.FlowFinScopeTabs
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSegmentedControl
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.component.FlowFinTileIcon
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import kotlinx.datetime.Month

private val HORIZONTAL = 24.dp

@Composable
fun AddRecurringScreen(
  state: AddRecurringUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onSelectType: (RecurringEntryType) -> Unit = {},
  onKey: (CalculatorKey) -> Unit = {},
  onNameChange: (String) -> Unit = {},
  onSelectFrequency: (RecurringFrequency) -> Unit = {},
  onOpenSheet: (AddRecurringSheet) -> Unit = {},
  onFocusAmount: () -> Unit = {},
  onBlurAmount: () -> Unit = {},
  onDismissSheet: () -> Unit = {},
  onPickWeekday: (Int) -> Unit = {},
  onPickMonthDay: (Int) -> Unit = {},
  onPickYearlyMonth: (Int) -> Unit = {},
  onPickYearlyDay: (Int) -> Unit = {},
  onPickAccount: (AccountId) -> Unit = {},
  onPickCategory: (CategoryId) -> Unit = {},
  onNoteChange: (String) -> Unit = {},
  onSave: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  val focusManager = LocalFocusManager.current
  val focusAmount = {
    focusManager.clearFocus()
    onFocusAmount()
  }
  val openSheet = { sheet: AddRecurringSheet ->
    focusManager.clearFocus()
    onOpenSheet(sheet)
  }
  val tint = if (state.type == RecurringEntryType.Income) palette.positive else palette.accent

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = stringResource(R.string.add_recurring_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
    bottomBar = {
      FlowFinFormDock(
        saveLabel = stringResource(R.string.add_recurring_save),
        onSave = onSave,
        padVisible = state.amountFocused,
        onHidePad = onBlurAmount,
        blockedReason = state.blockedReason?.let { stringResource(it) },
        saving = state.submitting,
        pad = { FlowFinCalculatorPad(onKey = onKey, tint = tint, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) },
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
    ) {
      FlowFinScopeTabs(
        options = RecurringEntryType.entries,
        selected = state.type,
        onSelect = onSelectType,
        label = { type ->
          stringResource(
            when (type) {
              RecurringEntryType.Expense -> R.string.add_tx_type_expense
              RecurringEntryType.Income -> R.string.add_tx_type_income
            },
          )
        },
        indicatorColor = { type ->
          if (type == RecurringEntryType.Income) palette.positive else palette.accent
        },
        modifier = Modifier.padding(horizontal = HORIZONTAL, vertical = 8.dp),
      )

      FlowFinAmountField(
        whole = state.amountWhole,
        label = stringResource(R.string.add_tx_field_amount),
        focused = state.amountFocused,
        onFocus = focusAmount,
        decimal = state.amountDecimal,
        expression = state.expression,
        tint = tint,
        empty = state.amount == null,
        modifier = Modifier.padding(horizontal = HORIZONTAL),
      )

      Column(Modifier.padding(horizontal = HORIZONTAL)) {
        FlowFinTextField(
          value = state.name,
          onValueChange = onNameChange,
          label = stringResource(R.string.add_recurring_field_name),
          placeholder = stringResource(R.string.add_recurring_hint_name),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
          ),
        )

        Spacer(Modifier.height(8.dp))
        // Cadence and its day were a segmented control plus a bespoke row; both are
        // choices, so both are rows that open the same picker every other choice does.
        val frequencyLabel = stringResource(
          when (state.frequency) {
            RecurringFrequency.Weekly -> R.string.recurring_freq_weekly
            RecurringFrequency.Monthly -> R.string.recurring_freq_monthly
            RecurringFrequency.Yearly -> R.string.recurring_freq_yearly
          },
        )
        FlowFinFormRow(
          label = stringResource(R.string.add_recurring_field_repeats),
          value = frequencyLabel + " · " + state.repeatsOn.asString(),
          valueSub = state.nextDue.asString(),
          onClick = { openSheet(AddRecurringSheet.RepeatsOn) },
        )

        val selectedAccount = state.selectedAccount()
        FlowFinFormRow(
          label = stringResource(
            if (state.type == RecurringEntryType.Income) R.string.add_tx_field_to else R.string.add_tx_field_from,
          ),
          value = selectedAccount?.name,
          placeholder = stringResource(R.string.add_tx_hint_account),
          leadingIcon = selectedAccount?.let { OptionTile(it.iconKey, it.colorKey) },
          auxText = selectedAccount?.balance,
          onClick = { openSheet(AddRecurringSheet.Account) },
        )
        val selectedCategory = state.selectedCategory()
        FlowFinFormRow(
          label = stringResource(R.string.add_tx_field_category),
          value = selectedCategory?.name,
          placeholder = stringResource(R.string.add_tx_hint_category),
          leadingIcon = selectedCategory?.let { OptionTile(it.iconKey, it.colorKey) },
          onClick = { openSheet(AddRecurringSheet.Category) },
        )
        FlowFinFormRow(
          label = stringResource(R.string.add_tx_field_note),
          value = state.note.ifBlank { null },
          placeholder = stringResource(R.string.add_tx_hint_note),
          trailingChevron = false,
          onClick = { openSheet(AddRecurringSheet.Note) },
        )
      }
    }
  }

  when (state.openSheet) {
    AddRecurringSheet.Amount -> AmountSheet(state, tint, onDismissSheet, onKey)
    AddRecurringSheet.RepeatsOn -> RepeatsOnSheet(state, onDismissSheet, onSelectFrequency, onPickWeekday, onPickMonthDay, onPickYearlyMonth, onPickYearlyDay)
    AddRecurringSheet.Account -> AccountSheet(state, onDismissSheet, onPickAccount)
    AddRecurringSheet.Category -> CategorySheet(state, onDismissSheet, onPickCategory)
    AddRecurringSheet.Note -> NoteSheet(state.note, onDismissSheet, onNoteChange)
    null -> Unit
  }
}

/** The chosen icon+tint tile on a form row, or null when nothing is picked yet. */
private fun OptionTile(iconKey: String?, colorKey: String?): @Composable () -> Unit = {
  FlowFinTileIcon(
    icon = categoryIcon(iconKey),
    tint = categoryColor(colorKey),
    size = 28.dp,
  )
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

/** The cadence summary ("25th of every month") over its next-firing preview. */
@Composable
private fun RepeatsOnRow(value: String, nextDue: String, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        text = value,
        style = FlowFinTheme.typography.bodyLg,
        color = palette.text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = nextDue,
        modifier = Modifier.padding(top = 4.dp),
        style = FlowFinTheme.typography.caption,
        color = palette.textSoft,
      )
    }
    Icon(
      imageVector = Icons.Rounded.ChevronRight,
      contentDescription = null,
      tint = palette.textFaint,
      modifier = Modifier.size(14.dp),
    )
  }
}

@Composable
private fun AmountSheet(
  state: AddRecurringUiState,
  tint: Color,
  onDismiss: () -> Unit,
  onKey: (CalculatorKey) -> Unit,
) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(
      title = stringResource(R.string.add_recurring_sheet_amount),
      onClose = onDismiss,
      actionLabel = stringResource(R.string.add_tx_note_done),
      onAction = onDismiss,
    )
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      FlowFinHeroAmount(
        whole = state.amountWhole,
        decimal = state.amountDecimal,
        expression = state.expression,
        tint = tint,
      )
    }
    FlowFinCalculatorPad(
      onKey = onKey,
      tint = tint,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
    )
  }
}

@Composable
private fun RepeatsOnSheet(
  state: AddRecurringUiState,
  onDismiss: () -> Unit,
  onSelectFrequency: (RecurringFrequency) -> Unit,
  onPickWeekday: (Int) -> Unit,
  onPickMonthDay: (Int) -> Unit,
  onPickYearlyMonth: (Int) -> Unit,
  onPickYearlyDay: (Int) -> Unit,
) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.add_recurring_field_repeats), onClose = onDismiss)

    // Cadence and the day within it are one decision — the row that opens this sheet
    // shows them as one value ("Monthly · 1st"), so the sheet has to set both halves.
    val frequencyLabels = RecurringFrequency.entries.associateWith { freq ->
      stringResource(
        when (freq) {
          RecurringFrequency.Weekly -> R.string.recurring_freq_weekly
          RecurringFrequency.Monthly -> R.string.recurring_freq_monthly
          RecurringFrequency.Yearly -> R.string.recurring_freq_yearly
        },
      )
    }
    FlowFinSegmentedControl(
      options = RecurringFrequency.entries,
      selected = state.frequency,
      onSelect = onSelectFrequency,
      label = { frequencyLabels.getValue(it) },
      modifier = Modifier.padding(horizontal = HORIZONTAL).padding(top = 4.dp, bottom = 16.dp),
    )

    when (state.frequency) {
      RecurringFrequency.Weekly -> Column(Modifier.padding(bottom = 8.dp)) {
        (1..7).forEach { iso ->
          WeekdayRow(
            name = weekdayFull(iso),
            selected = state.weeklyDay == iso,
            onClick = { onPickWeekday(iso) },
          )
        }
      }

      RecurringFrequency.Monthly -> DayGrid(
        selected = state.monthlyDay,
        onPick = onPickMonthDay,
        modifier = Modifier
          .padding(horizontal = HORIZONTAL)
          .padding(bottom = 16.dp),
      )

      RecurringFrequency.Yearly -> Column(
        Modifier
          .padding(horizontal = HORIZONTAL)
          .padding(bottom = 16.dp),
      ) {
        SectionLabel(stringResource(R.string.add_recurring_sheet_month), Modifier.padding(bottom = 10.dp))
        MonthGrid(selected = state.yearlyMonth, onPick = onPickYearlyMonth)
        SectionLabel(stringResource(R.string.add_recurring_sheet_day), Modifier.padding(top = 20.dp, bottom = 10.dp))
        DayGrid(selected = state.yearlyDay, onPick = onPickYearlyDay)
      }
    }
  }
}

@Composable
private fun WeekdayRow(name: String, selected: Boolean, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = HORIZONTAL, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = name,
      modifier = Modifier.weight(1f),
      style = FlowFinTheme.typography.bodyLg,
      color = palette.text,
    )
    if (selected) {
      Icon(
        imageVector = Icons.Rounded.Check,
        contentDescription = null,
        tint = palette.accent,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

/** 1–31 in calendar-style circles; a day past a month's end clamps at firing time. */
@Composable
private fun DayGrid(selected: Int, onPick: (Int) -> Unit, modifier: Modifier = Modifier) {
  val palette = FlowFinTheme.colors
  Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
    (1..31).chunked(7).forEach { week ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        week.forEach { day ->
          val isSelected = day == selected
          Box(
            modifier = Modifier
              .weight(1f)
              .aspectRatio(1f)
              .clip(CircleShape)
              .background(if (isSelected) palette.accent else Color.Transparent)
              .clickable { onPick(day) },
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = day.toString(),
              style = FlowFinTheme.typography.monoNum.copy(
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
              ),
              color = if (isSelected) palette.bg else palette.text,
            )
          }
        }
        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
      }
    }
  }
}

@Composable
private fun MonthGrid(selected: Int, onPick: (Int) -> Unit, modifier: Modifier = Modifier) {
  val palette = FlowFinTheme.colors
  Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    (1..12).chunked(4).forEach { row ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        row.forEach { month ->
          val isSelected = month == selected
          val shape = RoundedCornerShape(10.dp)
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(shape)
              .border(1.dp, if (isSelected) palette.accent else palette.borderStrong, shape)
              .background(if (isSelected) palette.accent.copy(alpha = 0.10f) else Color.Transparent)
              .clickable { onPick(month) }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = monthShort(Month(month)).uppercase(),
              style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.14.em),
              color = if (isSelected) palette.accent else palette.textSoft,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AccountSheet(
  state: AddRecurringUiState,
  onDismiss: () -> Unit,
  onPick: (AccountId) -> Unit,
) {
  val income = state.type == RecurringEntryType.Income
  // Income must land in a real account; an expense can draw from a budget too.
  val options = state.accountOptions.filter { !income || !it.isBudget }
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(
      title = stringResource(if (income) R.string.add_tx_sheet_to_account else R.string.add_tx_sheet_from_account),
      onClose = onDismiss,
    )
    options.forEach { option ->
      val tint = if (option.colorKey != null) categoryColor(option.colorKey) else null
      FlowFinPickerRow(
        icon = categoryIcon(option.iconKey),
        name = option.name,
        selected = option.id == state.account,
        onClick = { onPick(option.id) },
        amount = option.balance,
        tint = tint,
      )
    }
  }
}

@Composable
private fun CategorySheet(
  state: AddRecurringUiState,
  onDismiss: () -> Unit,
  onPick: (CategoryId) -> Unit,
) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.add_tx_field_category), onClose = onDismiss)
    state.categoryOptions.forEach { option ->
      val tint = if (option.colorKey != null) categoryColor(option.colorKey) else null
      FlowFinPickerRow(
        icon = categoryIcon(option.iconKey),
        name = option.name,
        selected = option.id == state.category,
        onClick = { onPick(option.id) },
        tint = tint,
      )
    }
  }
}

@Composable
private fun NoteSheet(note: String, onDismiss: () -> Unit, onChange: (String) -> Unit) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(
      title = stringResource(R.string.add_tx_field_note),
      onClose = onDismiss,
      actionLabel = stringResource(R.string.add_tx_note_done),
      onAction = onDismiss,
    )
    FlowFinTextField(
      value = note,
      onValueChange = onChange,
      placeholder = stringResource(R.string.add_tx_hint_note),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = HORIZONTAL, vertical = 8.dp),
    )
  }
}

@Preview(name = "Add recurring · empty", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAddRecurringEmpty() = FlowFinTheme {
  AddRecurringScreen(
    state = AddRecurringUiState(
      weeklyDay = 1,
      monthlyDay = 25,
      yearlyMonth = 6,
      yearlyDay = 25,
      repeatsOn = UiText.Raw("25th of every month"),
      nextDue = UiText.Raw("Next: 25 Jun 2026 · in 30 days"),
    ),
    snackbarHostState = remember { SnackbarHostState() },
  )
}

@Preview(name = "Add recurring · filled", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAddRecurringFilled() = FlowFinTheme {
  AddRecurringScreen(
    state = AddRecurringUiState(
      weeklyDay = 1,
      monthlyDay = 25,
      yearlyMonth = 6,
      yearlyDay = 25,
      amountWhole = "5,000",
      amountDecimal = ".00",
      name = "Gym Membership",
      repeatsOn = UiText.Raw("25th of every month"),
      nextDue = UiText.Raw("Next: 25 Jun 2026 · in 30 days"),
      note = "Monthly gym fees",
    ),
    snackbarHostState = remember { SnackbarHostState() },
  )
}
