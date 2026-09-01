@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.FlowFinAmountField
import com.flowfin.core.designsystem.component.FlowFinCalculatorPad
import com.flowfin.core.designsystem.component.FlowFinCalendar
import com.flowfin.core.designsystem.component.FlowFinFormDock
import com.flowfin.core.designsystem.component.FlowFinFormRow
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinPersonAvatar
import com.flowfin.core.designsystem.component.FlowFinPickerRow
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSettingsCard
import com.flowfin.core.designsystem.component.FlowFinSettingsToggleRow
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private val HORIZONTAL = 24.dp

/**
 * Record a payment against a debt.
 *
 * A screen rather than a bottom sheet: it has an amount, an optional account, a date
 * and a note, and as a sheet it had to open further sheets to be usable — while the
 * system keyboard, raised for the amount, covered everything beneath it. Same
 * anatomy as the other forms, so the keypad and Save share one pinned dock.
 */
@Composable
fun RecordPaymentScreen(
  state: RecordPaymentUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onKey: (CalculatorKey) -> Unit = {},
  onFocusAmount: () -> Unit = {},
  onBlurAmount: () -> Unit = {},
  onLinkAccountChange: (Boolean) -> Unit = {},
  onAccountSelected: (AccountId) -> Unit = {},
  onNoteChange: (String) -> Unit = {},
  onOpenSheet: (RecordPaymentSheet) -> Unit = {},
  onDismissSheet: () -> Unit = {},
  onPickDate: (LocalDate) -> Unit = {},
  onSave: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  val focusManager = LocalFocusManager.current
  val focusAmount = {
    focusManager.clearFocus()
    onFocusAmount()
  }

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = state.title.asString(),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
    bottomBar = {
      FlowFinFormDock(
        saveLabel = state.saveLabel.asString(),
        onSave = onSave,
        padVisible = state.amountFocused,
        onHidePad = onBlurAmount,
        blockedReason = state.blockedReason?.let { stringResource(it) },
        saving = state.saving,
        pad = { FlowFinCalculatorPad(onKey = onKey, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) },
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = HORIZONTAL),
    ) {
      Spacer(Modifier.height(8.dp))
      DebtContext(state)

      FlowFinAmountField(
        whole = state.amountWhole,
        label = state.amountLabel.asString(),
        focused = state.amountFocused,
        onFocus = focusAmount,
        currency = state.currency,
        decimal = state.amountDecimal,
        expression = state.expression,
        empty = state.amount == null,
      )

      // Where the debt lands if this saves — the reason anyone opens this screen.
      if (state.afterRemaining != null) {
        Text(
          text = state.afterRemaining.asString(),
          modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
          style = FlowFinTheme.typography.body,
          color = palette.textSoft,
        )
      }

      Spacer(Modifier.height(20.dp))
      FlowFinSettingsCard {
        FlowFinSettingsToggleRow(
          name = state.linkLabel.asString(),
          sub = state.linkDescription.asString(),
          checked = state.linkAccount,
          onCheckedChange = {
            focusManager.clearFocus()
            onBlurAmount()
            onLinkAccountChange(it)
          },
        )
      }

      when {
        !state.linkAccount -> Hint(stringResource(R.string.debt_detail_sheet_link_off))
        state.accounts.isEmpty() -> Hint(stringResource(R.string.debt_detail_sheet_no_accounts))
        else -> Column(
          modifier = Modifier.padding(top = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          state.accounts.forEach { account ->
            FlowFinPickerRow(
              icon = categoryIcon(account.iconKey),
              name = account.name,
              selected = account.id == state.selectedAccountId,
              onClick = { onAccountSelected(account.id) },
              amount = account.balance,
              tint = categoryColor(account.colorKey),
            )
          }
        }
      }

      Spacer(Modifier.height(12.dp))
      FlowFinFormRow(
        label = stringResource(R.string.debt_detail_sheet_date),
        value = state.dateLabel.asString(),
        onClick = {
          focusManager.clearFocus()
          onOpenSheet(RecordPaymentSheet.Date)
        },
      )
      FlowFinTextField(
        value = state.note,
        onValueChange = onNoteChange,
        label = stringResource(R.string.add_tx_field_note),
        placeholder = stringResource(R.string.add_tx_hint_note),
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      )
      Spacer(Modifier.height(24.dp))
    }
  }

  if (state.openSheet == RecordPaymentSheet.Date && state.date != null) {
    DateSheet(state.date, onDismissSheet, onPickDate)
  }
}

/** Who the debt is with and what is left on it — the context the amount is against. */
@Composable
private fun DebtContext(state: RecordPaymentUiState) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .padding(horizontal = 14.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FlowFinPersonAvatar(
      initial = state.personName.take(1).uppercase(),
      tint = palette.avatars.byIndex(state.avatarTintIndex),
    )
    Column(Modifier.weight(1f).padding(start = 12.dp)) {
      Text(state.personName, style = FlowFinTheme.typography.bodyLg, color = palette.text)
      if (state.reason != null) {
        Text(
          text = "“${state.reason}”",
          modifier = Modifier.padding(top = 2.dp),
          style = FlowFinTheme.typography.body.copy(fontSize = 13.sp),
          color = palette.textSoft,
        )
      }
    }
    Column(horizontalAlignment = Alignment.End) {
      Text(state.remainingWhole + state.remainingDecimal, style = FlowFinTheme.typography.bodyLg, color = palette.text)
      Text(
        text = state.remainingLabel.asString(),
        modifier = Modifier.padding(top = 2.dp),
        style = FlowFinTheme.typography.caption,
        color = palette.textSoft,
      )
    }
  }
}

@Composable
private fun Hint(text: String) {
  Text(
    text = text,
    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
    style = FlowFinTheme.typography.caption,
    color = FlowFinTheme.colors.textFaint,
  )
}

@Composable
private fun DateSheet(date: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.debt_detail_sheet_date), onClose = onDismiss)
    var month by remember { mutableStateOf(date) }
    FlowFinCalendar(
      month = month,
      onPreviousMonth = { month = month.minus(1, DateTimeUnit.MONTH) },
      onNextMonth = { month = month.plus(1, DateTimeUnit.MONTH) },
      onSelectDate = onPick,
      selectedDate = date,
      modifier = Modifier.padding(16.dp),
    )
  }
}

@Preview(name = "Record payment", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecordPayment() = FlowFinTheme {
  RecordPaymentScreen(
    state = RecordPaymentUiState(
      loading = false,
      personName = "Hina",
      reason = "Cash from Ammi",
      remainingWhole = "8,000",
      amountWhole = "4,000",
      date = LocalDate(2026, 9, 1),
    ),
    snackbarHostState = remember { SnackbarHostState() },
  )
}
