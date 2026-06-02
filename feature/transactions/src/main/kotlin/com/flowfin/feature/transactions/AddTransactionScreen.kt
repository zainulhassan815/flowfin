@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinCalculatorPad
import com.flowfin.core.designsystem.component.FlowFinCalendar
import com.flowfin.core.designsystem.component.FlowFinFormRow
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinPickerRow
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.component.FlowFinScopeTabs
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@Composable
fun AddTransactionScreen(
  state: AddTransactionUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onSelectType: (EntryType) -> Unit = {},
  onKey: (CalculatorKey) -> Unit = {},
  onOpenSheet: (AddSheet) -> Unit = {},
  onDismissSheet: () -> Unit = {},
  onPickFromAccount: (AccountId) -> Unit = {},
  onPickToAccount: (AccountId) -> Unit = {},
  onPickCategory: (CategoryId) -> Unit = {},
  onPickDate: (LocalDate) -> Unit = {},
  onNoteChange: (String) -> Unit = {},
  onSave: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  val tint = when (state.type) {
    EntryType.Expense -> palette.accent
    EntryType.Income -> palette.positive
    EntryType.Transfer -> palette.transfer
  }

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = { FlowFinPageHeader(title = "Add", onBack = onBack) },
    bottomBar = {
      FlowFinButton(
        onClick = onSave,
        text = "Save",
        enabled = state.canSave,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(top = 8.dp, bottom = 12.dp),
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
        options = EntryType.entries,
        selected = state.type,
        onSelect = onSelectType,
        label = { it.name },
        indicatorColor = { type ->
          when (type) {
            EntryType.Expense -> palette.accent
            EntryType.Income -> palette.positive
            EntryType.Transfer -> palette.transfer
          }
        },
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
      )

      FlowFinHeroAmount(
        whole = state.amountWhole,
        decimal = state.amountDecimal,
        expression = state.expression,
        tint = tint,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
      )

      Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        when (state.type) {
          EntryType.Expense -> {
            AccountRow("From", state.selectedFrom()) { onOpenSheet(AddSheet.FromAccount) }
            CategoryRow(state.selectedCategory()) { onOpenSheet(AddSheet.Category) }
          }
          EntryType.Income -> {
            AccountRow("To", state.selectedTo()) { onOpenSheet(AddSheet.ToAccount) }
            CategoryRow(state.selectedCategory()) { onOpenSheet(AddSheet.Category) }
          }
          EntryType.Transfer -> {
            AccountRow("From", state.selectedFrom()) { onOpenSheet(AddSheet.FromAccount) }
            AccountRow("To", state.selectedTo()) { onOpenSheet(AddSheet.ToAccount) }
          }
        }
        FlowFinFormRow(label = "Date", value = formatDate(state.date), onClick = { onOpenSheet(AddSheet.Date) })
        FlowFinFormRow(label = "Note", value = state.note.ifBlank { null }, onClick = { onOpenSheet(AddSheet.Note) })
      }

      FlowFinCalculatorPad(
        onKey = onKey,
        tint = tint,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      )
    }
  }

  when (state.openSheet) {
    AddSheet.FromAccount -> AccountSheet("From account", state.accountOptions.filter { state.type != EntryType.Transfer || !it.isBudget }, state.fromAccount.value, onDismissSheet, onPickFromAccount)
    AddSheet.ToAccount -> AccountSheet("To account", state.accountOptions.filter { !it.isBudget }, state.toAccount.value, onDismissSheet, onPickToAccount)
    AddSheet.Category -> CategorySheet(state, onDismissSheet, onPickCategory)
    AddSheet.Date -> DateSheet(state.date, onDismissSheet, onPickDate)
    AddSheet.Note -> NoteSheet(state.note, onDismissSheet, onNoteChange)
    null -> Unit
  }
}

@Composable
private fun AccountRow(label: String, selected: AccountOption?, onClick: () -> Unit) {
  FlowFinFormRow(label = label, value = selected?.name, onClick = onClick)
}

@Composable
private fun CategoryRow(selected: CategoryOption?, onClick: () -> Unit) {
  FlowFinFormRow(label = "Category", value = selected?.name, onClick = onClick)
}

@Composable
private fun AccountSheet(
  title: String,
  options: List<AccountOption>,
  selectedId: AccountId?,
  onDismiss: () -> Unit,
  onPick: (AccountId) -> Unit,
) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = title, onClose = onDismiss)
    options.forEach { option ->
      val tint = if (option.colorKey != null) categoryColor(option.colorKey) else null
      FlowFinPickerRow(
        icon = categoryIcon(option.iconKey),
        name = option.name,
        selected = option.id == selectedId,
        onClick = { onPick(option.id) },
        amount = option.balance,
        tint = tint,
      )
    }
  }
}

@Composable
private fun CategorySheet(state: AddTransactionUiState, onDismiss: () -> Unit, onPick: (CategoryId) -> Unit) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = "Category", onClose = onDismiss)
    state.categoryOptions.forEach { option ->
      val tint = if (option.colorKey != null) categoryColor(option.colorKey) else null
      FlowFinPickerRow(
        icon = categoryIcon(option.iconKey),
        name = option.name,
        selected = option.id == state.category.value,
        onClick = { onPick(option.id) },
        tint = tint,
      )
    }
  }
}

@Composable
private fun DateSheet(date: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = "Date", onClose = onDismiss)
    var month by remember { mutableStateOf(date) }
    FlowFinCalendar(
      month = month,
      onPreviousMonth = { month = month.minusMonth() },
      onNextMonth = { month = month.plus(1, DateTimeUnit.MONTH) },
      onSelectDate = onPick,
      selectedDate = date,
      modifier = Modifier.padding(16.dp),
    )
  }
}

@Composable
private fun NoteSheet(note: String, onDismiss: () -> Unit, onChange: (String) -> Unit) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = "Note", onClose = onDismiss, actionLabel = "Done", onAction = onDismiss)
    FlowFinTextField(
      value = note,
      onValueChange = onChange,
      placeholder = "What was this for?",
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp),
    )
  }
}

private fun LocalDate.minusMonth(): LocalDate = plus(-1, DateTimeUnit.MONTH)

private fun formatDate(date: LocalDate): String =
  "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)} ${date.dayOfMonth}, ${date.year}"
