@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPersonAvatar
import com.flowfin.core.designsystem.component.FlowFinPickerRow
import com.flowfin.core.designsystem.component.FlowFinSettingsCard
import com.flowfin.core.designsystem.component.FlowFinSettingsToggleRow
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

/**
 * Records a repayment (or receipt) against one debt. The amount is a digit
 * buffer typed right-to-left into minor units — an invisible text field drives
 * the keyboard while the hero renders the formatted figure, so there's no
 * decimal point to parse and no half-entered value.
 *
 * Linking an account is optional: off, the debt still moves but no account
 * balance does, which is how an off-book repayment is recorded.
 */
@Composable
fun RecordPaymentSheet(
  state: RecordPaymentUi,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  onAmountDigits: (String) -> Unit = {},
  onLinkAccountChange: (Boolean) -> Unit = {},
  onAccountSelected: (AccountId) -> Unit = {},
  onNoteChange: (String) -> Unit = {},
  onSave: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  FlowFinModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
    Text(
      text = state.title.asString(),
      modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL),
      style = FlowFinTheme.typography.h2.copy(fontSize = 18.sp),
      color = palette.text,
    )

    Column(
      modifier = Modifier
        .weight(1f, fill = false)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = HORIZONTAL),
    ) {
      DebtContextCard(state)
      AmountEntry(state, onAmountDigits)
      LinkSection(state, onLinkAccountChange, onAccountSelected)
      DateAndNote(state, onNoteChange)
    }

    FlowFinButton(
      onClick = onSave,
      text = state.saveLabel.asString(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = HORIZONTAL, top = 16.dp, end = HORIZONTAL, bottom = 12.dp),
      enabled = state.canSave,
    )
  }
}

@Composable
private fun DebtContextCard(state: RecordPaymentUi) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp)
      .background(palette.surface, RoundedCornerShape(14.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FlowFinPersonAvatar(
      initial = state.personName.take(1).uppercase(),
      tint = palette.avatars.byIndex(state.avatarTintIndex),
    )
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
      Text(
        text = state.personName,
        style = FlowFinTheme.typography.bodyLg,
        color = palette.text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (state.reason != null) {
        Text(
          text = "“${state.reason}”",
          style = FlowFinTheme.typography.body.copy(fontSize = 13.sp, fontStyle = FontStyle.Italic),
          color = palette.textSoft,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    Spacer(Modifier.width(8.dp))
    Column(horizontalAlignment = Alignment.End) {
      Row(verticalAlignment = Alignment.Bottom) {
        Text(state.remainingWhole, style = FlowFinTheme.typography.monoNum.copy(fontSize = 16.sp), color = palette.text)
        Text(state.remainingDecimal, style = FlowFinTheme.typography.monoNum.copy(fontSize = 12.sp), color = palette.textFaint)
      }
      Text(state.remainingLabel.asString(), style = FlowFinTheme.typography.caption, color = palette.textFaint)
    }
  }
}

@Composable
private fun AmountEntry(state: RecordPaymentUi, onAmountDigits: (String) -> Unit) {
  val palette = FlowFinTheme.colors
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = state.amountLabel.asString().uppercase(),
      style = FlowFinTheme.typography.caption,
      color = palette.textFaint,
    )
    FlowFinHeroAmount(whole = state.amountWhole, decimal = state.amountDecimal)
    // The field itself is never seen — it exists to own the keyboard and the
    // digit buffer while the hero above renders the formatted figure.
    BasicTextField(
      value = TextFieldValue(state.amountDigits, TextRange(state.amountDigits.length)),
      onValueChange = { onAmountDigits(it.text) },
      modifier = Modifier.focusRequester(focusRequester).heightIn(max = 1.dp).fillMaxWidth(0.01f),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
      textStyle = FlowFinTheme.typography.caption.copy(color = Color.Transparent),
      cursorBrush = SolidColor(Color.Transparent),
    )
    if (state.afterRemaining != null) {
      Text(
        text = state.afterRemaining.asString(),
        modifier = Modifier.padding(top = 8.dp),
        style = FlowFinTheme.typography.caption,
        color = palette.textSoft,
      )
    }
  }
}

@Composable
private fun LinkSection(
  state: RecordPaymentUi,
  onLinkAccountChange: (Boolean) -> Unit,
  onAccountSelected: (AccountId) -> Unit,
) {
  Column(Modifier.padding(top = 24.dp)) {
    FlowFinSettingsCard {
      FlowFinSettingsToggleRow(
        name = state.linkLabel.asString(),
        sub = state.linkDescription.asString(),
        checked = state.linkAccount,
        onCheckedChange = onLinkAccountChange,
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
  }
}

@Composable
private fun Hint(text: String) {
  Text(
    text = text,
    modifier = Modifier.padding(top = 10.dp),
    style = FlowFinTheme.typography.caption,
    color = FlowFinTheme.colors.textSoft,
  )
}

@Composable
private fun DateAndNote(state: RecordPaymentUi, onNoteChange: (String) -> Unit) {
  val palette = FlowFinTheme.colors
  Column(Modifier.padding(top = 24.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(stringResource(R.string.debt_detail_sheet_date), style = FlowFinTheme.typography.caption, color = palette.textFaint)
      Text(state.dateLabel.asString(), style = FlowFinTheme.typography.body.copy(fontSize = 13.sp), color = palette.textMute)
    }
    FlowFinTextField(
      value = state.note,
      onValueChange = onNoteChange,
      placeholder = stringResource(R.string.debt_detail_sheet_note_hint),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Preview(name = "Record payment", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecordPayment() = FlowFinTheme {
  RecordPaymentSheet(
    state = RecordPaymentUi(
      title = UiText.Raw("Record payment"),
      amountLabel = UiText.Raw("Payment amount"),
      amountDigits = "200000",
      amountWhole = "2,000",
      amountDecimal = ".00",
      personName = "Ahmed",
      avatarTintIndex = 1,
      reason = "Borrowed for rent",
      remainingWhole = "3,000",
      remainingDecimal = ".00",
      remainingLabel = UiText.Raw("Remaining"),
      afterRemaining = UiText.Raw("After this · Rs 1,000.00 remaining"),
      linkAccount = true,
      linkLabel = UiText.Raw("Create expense from account"),
      linkDescription = UiText.Raw("Records this in your expense history"),
      accounts = emptyList(),
      selectedAccountId = null,
      saveLabel = UiText.Raw("Save payment"),
      dateLabel = UiText.Raw("Today, 26 May 2026"),
      note = "",
      saving = false,
    ),
    onDismiss = {},
  )
}
