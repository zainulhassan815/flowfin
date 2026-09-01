@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.debts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinCalculatorPad
import com.flowfin.core.designsystem.component.FlowFinAmountField
import com.flowfin.core.designsystem.component.FlowFinFormDock
import com.flowfin.core.designsystem.component.FlowFinCalendar
import com.flowfin.core.designsystem.component.FlowFinFormRow
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinPersonAvatar
import com.flowfin.core.designsystem.component.FlowFinPickerRow
import com.flowfin.core.designsystem.component.FlowFinScopeTabs
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSettingsCard
import com.flowfin.core.designsystem.component.FlowFinSettingsToggleRow
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.component.FlowFinTileIcon
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.PersonId
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

@Composable
fun AddDebtScreen(
  state: AddDebtUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onSelectDirection: (DebtDirection) -> Unit = {},
  onKey: (CalculatorKey) -> Unit = {},
  onOpenSheet: (AddDebtSheet) -> Unit = {},
  onPickDate: (LocalDate) -> Unit = {},
  onFocusAmount: () -> Unit = {},
  onBlurAmount: () -> Unit = {},
  onDismissSheet: () -> Unit = {},
  onPersonQueryChange: (String) -> Unit = {},
  onPickPerson: (PersonId) -> Unit = {},
  onUseTypedPerson: () -> Unit = {},
  onReasonChange: (String) -> Unit = {},
  onLinkAccountChange: (Boolean) -> Unit = {},
  onPickAccount: (AccountId) -> Unit = {},
  onSave: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  val focusManager = LocalFocusManager.current
  val focusAmount = {
    focusManager.clearFocus()
    onFocusAmount()
  }
  val openSheet = { sheet: AddDebtSheet ->
    focusManager.clearFocus()
    onOpenSheet(sheet)
  }
  // Money coming in reads positive; money going out reads as the accent spend tone.
  val tint = if (state.isBorrowing) palette.positive else palette.accent

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = stringResource(R.string.add_debt_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
    bottomBar = {
      FlowFinFormDock(
        saveLabel = stringResource(R.string.add_debt_save),
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
        options = listOf(DebtDirection.I_OWE, DebtDirection.OWED_TO_ME),
        selected = state.direction,
        onSelect = onSelectDirection,
        label = {
          stringResource(if (it == DebtDirection.I_OWE) R.string.debts_tab_i_owe else R.string.debts_tab_owe_me)
        },
        indicatorColor = { if (it == DebtDirection.I_OWE) palette.positive else palette.accent },
        modifier = Modifier.padding(horizontal = HORIZONTAL, vertical = 8.dp),
      )

      FlowFinAmountField(
        whole = state.amountWhole,
        label = stringResource(
          if (state.isBorrowing) R.string.add_debt_stamp_borrowed else R.string.add_debt_stamp_lent,
        ),
        focused = state.amountFocused,
        onFocus = focusAmount,
        decimal = state.amountDecimal,
        expression = state.expression,
        tint = tint,
        empty = state.amount == null,
        modifier = Modifier.padding(horizontal = HORIZONTAL),
      )

      Column(Modifier.padding(horizontal = HORIZONTAL)) {
        FlowFinFormRow(
          label = stringResource(R.string.add_debt_field_person),
          value = state.personName,
          placeholder = stringResource(R.string.add_debt_person_placeholder),
          leadingIcon = state.personName?.let { name ->
            {
              FlowFinPersonAvatar(
                initial = name.take(1).uppercase(),
                tint = palette.avatars.byIndex(state.selectedPerson()?.avatarTintIndex ?: 1),
                modifier = Modifier.width(28.dp),
              )
            }
          },
          onClick = { openSheet(AddDebtSheet.Person) },
        )
        FlowFinFormRow(
          label = stringResource(R.string.add_debt_field_reason),
          value = state.reason.ifBlank { null },
          placeholder = stringResource(R.string.add_debt_reason_placeholder),
          trailingChevron = false,
          onClick = { openSheet(AddDebtSheet.Reason) },
        )
        FlowFinFormRow(
          label = stringResource(R.string.debt_detail_sheet_date),
          value = state.dateLabel.asString(),
          onClick = { openSheet(AddDebtSheet.Date) },
        )

        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = palette.border)

        FlowFinSettingsCard {
          FlowFinSettingsToggleRow(
            name = stringResource(
              if (state.isBorrowing) R.string.add_debt_link_label_borrow else R.string.add_debt_link_label_lend,
            ),
            sub = stringResource(
              if (state.isBorrowing) R.string.add_debt_link_desc_borrow else R.string.add_debt_link_desc_lend,
            ),
            checked = state.linkAccount,
            onCheckedChange = onLinkAccountChange,
          )
        }
        if (state.linkAccount) {
          val selected = state.selectedAccount()
          FlowFinFormRow(
            label = stringResource(if (state.isBorrowing) R.string.add_tx_field_to else R.string.add_tx_field_from),
            value = selected?.name,
            leadingIcon = selected?.let { OptionTile(it.iconKey, it.colorKey) },
            auxText = selected?.balance,
            onClick = { openSheet(AddDebtSheet.Account) },
          )
        } else {
          Text(
            text = stringResource(R.string.add_debt_link_off),
            modifier = Modifier.padding(top = 10.dp),
            style = FlowFinTheme.typography.caption,
            color = palette.textSoft,
          )
        }
      }
    }
  }

  when (state.openSheet) {
    AddDebtSheet.Date -> DebtDateSheet(state.date, onDismissSheet, onPickDate)
    AddDebtSheet.Amount -> AmountSheet(state, tint, onDismissSheet, onKey)
    AddDebtSheet.Person -> PersonSheet(state, onDismissSheet, onPersonQueryChange, onPickPerson, onUseTypedPerson)
    AddDebtSheet.Account -> AccountSheet(state, onDismissSheet, onPickAccount)
    AddDebtSheet.Reason -> ReasonSheet(state.reason, onDismissSheet, onReasonChange)
    null -> Unit
  }
}

/** The chosen icon+tint tile on a form row, or null when nothing is picked yet. */
private fun OptionTile(iconKey: String?, colorKey: String?): @Composable () -> Unit = {
  FlowFinTileIcon(icon = categoryIcon(iconKey), tint = categoryColor(colorKey), size = 28.dp)
}

@Composable
private fun AmountSheet(
  state: AddDebtUiState,
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

/**
 * Pick an existing contact, or type a new name and use it — a debt against
 * someone not in the list shouldn't need a detour through a separate screen.
 */
@Composable
private fun PersonSheet(
  state: AddDebtUiState,
  onDismiss: () -> Unit,
  onQueryChange: (String) -> Unit,
  onPick: (PersonId) -> Unit,
  onUseTyped: () -> Unit,
) {
  val palette = FlowFinTheme.colors
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.add_debt_field_person), onClose = onDismiss)
    Column(Modifier.padding(horizontal = HORIZONTAL, vertical = 4.dp)) {
      FlowFinTextField(
        value = state.personQuery,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.add_debt_person_search),
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          imeAction = ImeAction.Done,
        ),
      )
      if (state.canCreatePerson) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUseTyped)
            .padding(vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          FlowFinPersonAvatar(initial = state.personQuery.trim().take(1).uppercase(), tint = palette.accent)
          Spacer(Modifier.width(12.dp))
          Text(
            text = stringResource(R.string.add_debt_person_create, state.personQuery.trim()),
            style = FlowFinTheme.typography.bodyLg,
            color = palette.text,
          )
        }
      }
      Column(
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        state.filteredPersons.forEach { option ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onPick(option.id) }
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            FlowFinPersonAvatar(
              initial = option.name.take(1).uppercase(),
              tint = palette.avatars.byIndex(option.avatarTintIndex),
            )
            Spacer(Modifier.width(12.dp))
            Text(option.name, style = FlowFinTheme.typography.bodyLg, color = palette.text)
          }
        }
      }
    }
  }
}

@Composable
private fun AccountSheet(state: AddDebtUiState, onDismiss: () -> Unit, onPick: (AccountId) -> Unit) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(
      title = stringResource(if (state.isBorrowing) R.string.add_tx_field_to else R.string.add_tx_field_from),
      onClose = onDismiss,
    )
    Column(
      modifier = Modifier.padding(horizontal = HORIZONTAL).padding(bottom = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      state.accountOptions.forEach { option ->
        FlowFinPickerRow(
          icon = categoryIcon(option.iconKey),
          name = option.name,
          selected = option.id == state.account,
          onClick = { onPick(option.id) },
          amount = option.balance,
          tint = categoryColor(option.colorKey),
        )
      }
    }
  }
}

@Composable
private fun ReasonSheet(reason: String, onDismiss: () -> Unit, onChange: (String) -> Unit) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(
      title = stringResource(R.string.add_debt_field_reason),
      onClose = onDismiss,
      actionLabel = stringResource(R.string.add_tx_note_done),
      onAction = onDismiss,
    )
    FlowFinTextField(
      value = reason,
      onValueChange = onChange,
      placeholder = stringResource(R.string.add_debt_reason_placeholder),
      modifier = Modifier.padding(horizontal = HORIZONTAL).padding(bottom = 16.dp),
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
      ),
    )
  }
}

@Preview(name = "Add debt", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAddDebt() = FlowFinTheme {
  AddDebtScreen(
    state = AddDebtUiState(
      amountWhole = "5,000",
      amountDecimal = ".00",
      newPersonName = "Ahmed",
      reason = "Borrowed for rent",
      dateLabel = UiText.Raw("Sat · 27 Dec 2026"),
      linkAccount = false,
    ),
    snackbarHostState = remember { SnackbarHostState() },
  )
}

@Composable
private fun DebtDateSheet(date: LocalDate?, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
  val selected = date ?: return
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.debt_detail_sheet_date), onClose = onDismiss)
    var month by remember { mutableStateOf(selected) }
    FlowFinCalendar(
      month = month,
      onPreviousMonth = { month = month.minus(1, DateTimeUnit.MONTH) },
      onNextMonth = { month = month.plus(1, DateTimeUnit.MONTH) },
      onSelectDate = onPick,
      selectedDate = selected,
      modifier = Modifier.padding(16.dp),
    )
  }
}
