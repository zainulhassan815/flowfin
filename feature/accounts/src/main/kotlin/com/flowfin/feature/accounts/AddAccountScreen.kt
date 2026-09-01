package com.flowfin.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.FlowFinAmountField
import com.flowfin.core.designsystem.component.FlowFinCalculatorPad
import com.flowfin.core.designsystem.component.FlowFinFormDock
import com.flowfin.core.designsystem.component.FlowFinKeyGrid
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.resources.R
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import androidx.compose.ui.res.stringResource

private val HORIZONTAL = 24.dp

@Composable
fun AddAccountScreen(
  state: AddAccountUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onNameChange: (String) -> Unit = {},
  onSelectKind: (AccountKind) -> Unit = {},
  onSelectColor: (String) -> Unit = {},
  onKey: (CalculatorKey) -> Unit = {},
  onFocusAmount: () -> Unit = {},
  onBlurAmount: () -> Unit = {},
  onSave: () -> Unit = {},
) {
  val focusManager = LocalFocusManager.current
  val focusAmount = {
    focusManager.clearFocus()
    onFocusAmount()
  }
  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = stringResource(R.string.add_account_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
    bottomBar = {
      FlowFinFormDock(
        saveLabel = stringResource(R.string.add_account_submit),
        onSave = onSave,
        padVisible = state.amountFocused,
        onHidePad = onBlurAmount,
        blockedReason = state.blockedReason?.let { stringResource(it) },
        saving = state.submitting,
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
      FlowFinAmountField(
        whole = state.amountWhole,
        label = stringResource(R.string.add_account_balance_label),
        focused = state.amountFocused,
        onFocus = focusAmount,
        currency = state.currency,
        decimal = state.amountDecimal,
        expression = state.expression,
        empty = state.openingBalance == null,
      )
      Spacer(Modifier.height(12.dp))
      NameField(state, onNameChange)
      Spacer(Modifier.height(28.dp))
      KindChooser(state.kind, onSelectKind)
      Spacer(Modifier.height(28.dp))
      Text(
        text = stringResource(R.string.add_budget_colour_label).uppercase(),
        modifier = Modifier.padding(bottom = 10.dp),
        style = FlowFinTheme.typography.label,
        color = FlowFinTheme.colors.textSoft,
      )
      FlowFinKeyGrid(
        keys = ACCOUNT_COLOR_KEYS,
        selected = state.colorKey ?: state.kind.colorKey,
        ringShape = CircleShape,
        ringColor = categoryColor(state.colorKey ?: state.kind.colorKey),
        onSelect = onSelectColor,
      ) { key, _ ->
        Box(Modifier.size(34.dp).clip(CircleShape).background(categoryColor(key)))
      }
      Spacer(Modifier.height(24.dp))
    }
  }
}

/** Sticky footer: the primary action (disabled until the form is valid). */
@Composable
private fun BottomActions(state: AddAccountUiState, onSave: () -> Unit) {
  FlowFinButton(
    onClick = onSave,
    text = stringResource(R.string.add_account_submit),
    enabled = state.canSave,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = HORIZONTAL)
      .padding(top = 8.dp, bottom = 12.dp),
  )
}

@Composable
private fun NameField(state: AddAccountUiState, onNameChange: (String) -> Unit) {
  FlowFinTextField(
    value = state.name,
    onValueChange = onNameChange,
    label = stringResource(R.string.add_account_name_label),
    placeholder = stringResource(state.kind.nameHintRes),
    errorMessage = if (state.nameError == NameError.Taken) {
      stringResource(R.string.add_account_duplicate_name, state.name.trim())
    } else {
      null
    },
    keyboardOptions = KeyboardOptions(
      capitalization = KeyboardCapitalization.Words,
      imeAction = ImeAction.Done,
    ),
  )
}

@Composable
private fun KindChooser(selected: AccountKind, onSelectKind: (AccountKind) -> Unit) {
  Text(
    text = stringResource(R.string.add_account_kind_label).uppercase(),
    modifier = Modifier.padding(bottom = 10.dp),
    style = FlowFinTheme.typography.label,
    color = FlowFinTheme.colors.textSoft,
  )
  Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    AccountKind.entries.forEach { kind ->
      KindChip(
        kind = kind,
        selected = kind == selected,
        onClick = { onSelectKind(kind) },
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun KindChip(kind: AccountKind, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val palette = FlowFinTheme.colors
  val color = categoryColor(kind.colorKey)
  val shape = RoundedCornerShape(12.dp)
  Row(
    modifier = modifier
      .clip(shape)
      .border(1.dp, if (selected) color else palette.borderStrong, shape)
      .background(if (selected) color.copy(alpha = 0.07f) else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(vertical = 11.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = categoryIcon(kind.iconKey),
      contentDescription = null,
      tint = if (selected) color else palette.textSoft,
      modifier = Modifier.size(16.dp),
    )
    Spacer(Modifier.size(8.dp))
    Text(
      text = stringResource(kind.labelRes).uppercase(),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.14.em),
      color = if (selected) color else palette.textSoft,
    )
  }
}

@Composable
private fun BalanceField(state: AddAccountUiState, onBalanceChange: (String) -> Unit) {
  FlowFinTextField(
    value = state.balance,
    onValueChange = onBalanceChange,
    label = stringResource(R.string.add_account_balance_label),
    placeholder = "0.00",
    prefix = {
      Text(
        text = state.currency,
        style = FlowFinTheme.typography.h2.copy(fontSize = 18.sp),
        color = FlowFinTheme.colors.textMute,
      )
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
  )
}

@Preview(name = "Add account · empty", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAddAccountEmpty() = FlowFinTheme {
  AddAccountScreen(state = AddAccountUiState(currency = "Rs"), snackbarHostState = remember { SnackbarHostState() })
}

@Preview(name = "Add account · name taken", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAddAccountTaken() = FlowFinTheme {
  AddAccountScreen(
    state = AddAccountUiState(name = "Cash", kind = AccountKind.Cash, nameError = NameError.Taken, currency = "Rs"),
    snackbarHostState = remember { SnackbarHostState() },
  )
}

@Preview(name = "Add account · ready", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAddAccountReady() = FlowFinTheme {
  AddAccountScreen(
    state = AddAccountUiState(name = "HBL Savings", balance = "40000", nameError = null, currency = "Rs"),
    snackbarHostState = remember { SnackbarHostState() },
  )
}
