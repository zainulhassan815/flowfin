@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.FlowFinCalculatorPad
import com.flowfin.core.designsystem.component.FlowFinFormDock
import com.flowfin.core.designsystem.component.FlowFinFormRow
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinKeyGrid
import com.flowfin.core.designsystem.component.FlowFinKeyGridCellPadding
import com.flowfin.core.designsystem.component.FlowFinKeyGridTileRadius
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinPickerRow
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.FlowFinSwitch
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.component.FlowFinTileIcon
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

/**
 * Create a budget envelope, and give it the rhythm people actually budget in: a
 * monthly refill. The envelope stays the truth — the balance carries over — but a
 * declared monthly figure is what lets the rest of the app say "spent Rs 22,000 of
 * Rs 28,000 this month" instead of a lifetime total.
 *
 * Follows the form anatomy in `design/mockups/forms/index.html`: amount hero, one
 * field idiom, and a pinned dock whose keypad follows focus. This is a "thing you
 * name", so it opens on the name with the pad down.
 */
@Composable
fun AddBudgetScreen(
  state: AddBudgetUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onNameChange: (String) -> Unit = {},
  onSelectIcon: (String) -> Unit = {},
  onSelectColor: (String) -> Unit = {},
  onSelectParent: (AccountId) -> Unit = {},
  onToggleRefill: () -> Unit = {},
  onSelectRefillDay: (Int) -> Unit = {},
  onToggleFundNow: () -> Unit = {},
  onOpenSheet: (AddBudgetSheet) -> Unit = {},
  onDismissSheet: () -> Unit = {},
  onFocusAmount: () -> Unit = {},
  onBlurAmount: () -> Unit = {},
  onKey: (CalculatorKey) -> Unit = {},
  onSave: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  // The pad and the system keyboard occupy the same slot, so taking the amount's
  // focus has to give up the name field's — otherwise both are on screen at once.
  val focusManager = LocalFocusManager.current
  val focusAmount = {
    focusManager.clearFocus()
    onFocusAmount()
  }
  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = stringResource(R.string.add_budget_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
    bottomBar = {
      FlowFinFormDock(
        saveLabel = stringResource(R.string.add_budget_submit),
        onSave = onSave,
        padVisible = state.amountFocused,
        onHidePad = onBlurAmount,
        blockedReason = state.blockedReason?.let { stringResource(it) },
        saving = state.submitting,
        pad = { FlowFinCalculatorPad(onKey = onKey) },
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
      AmountHero(state, focusAmount)
      Spacer(Modifier.height(4.dp))

      FlowFinTextField(
        value = state.name,
        onValueChange = onNameChange,
        label = stringResource(R.string.add_budget_name_label),
        placeholder = stringResource(R.string.add_budget_name_hint),
        errorMessage = if (state.nameError == NameError.Taken) {
          stringResource(R.string.add_budget_duplicate_name, state.name.trim())
        } else {
          null
        },
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          imeAction = ImeAction.Done,
        ),
      )
      Spacer(Modifier.height(16.dp))

      FlowFinFormRow(
        label = stringResource(R.string.add_budget_parent_label),
        value = state.selectedParent?.name,
        placeholder = stringResource(R.string.add_budget_parent_hint),
        valueSub = stringResource(R.string.add_budget_parent_sub).takeIf { state.parent == null },
        auxText = state.selectedParent?.balance,
        leadingIcon = state.selectedParent?.let { parent ->
          {
            FlowFinTileIcon(
              icon = categoryIcon(parent.iconKey),
              tint = categoryColor(parent.colorKey),
              size = 28.dp,
            )
          }
        },
        onClick = { focusManager.clearFocus(); onOpenSheet(AddBudgetSheet.Parent) },
      )
      HorizontalDivider(color = palette.border)

      Spacer(Modifier.height(18.dp))
      PickerLabel(stringResource(R.string.add_budget_icon_label))
      FlowFinKeyGrid(
        keys = BUDGET_ICON_KEYS,
        selected = state.iconKey,
        ringShape = RoundedCornerShape(FlowFinKeyGridTileRadius + FlowFinKeyGridCellPadding),
        ringColor = categoryColor(state.colorKey),
        onSelect = onSelectIcon,
      ) { key, isSelected ->
        FlowFinTileIcon(
          icon = categoryIcon(key),
          tint = if (isSelected) categoryColor(state.colorKey) else palette.textSoft,
          size = 40.dp,
        )
      }

      PickerLabel(stringResource(R.string.add_budget_colour_label))
      FlowFinKeyGrid(
        keys = BUDGET_COLOR_KEYS,
        selected = state.colorKey,
        ringShape = CircleShape,
        ringColor = categoryColor(state.colorKey),
        onSelect = onSelectColor,
      ) { key, _ ->
        Box(Modifier.size(34.dp).clip(CircleShape).background(categoryColor(key)))
      }

      Spacer(Modifier.height(18.dp))
      RefillCard(state, onToggleRefill, onToggleFundNow) { focusManager.clearFocus(); onOpenSheet(AddBudgetSheet.RefillDay) }
      Spacer(Modifier.height(24.dp))
    }
  }

  when (state.openSheet) {
    AddBudgetSheet.Parent -> ParentSheet(state, onDismissSheet, onSelectParent)
    AddBudgetSheet.RefillDay -> RefillDaySheet(state.refillDay, onDismissSheet, onSelectRefillDay)
    null -> Unit
  }
}

/** The amount is a field, drawn like one: underline faint at rest, accent on focus. */
@Composable
private fun AmountHero(state: AddBudgetUiState, onFocus: () -> Unit) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onFocus)
      .padding(top = 12.dp, bottom = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.add_budget_amount_label).uppercase(),
      style = FlowFinTheme.typography.label,
      color = palette.textSoft,
    )
    Spacer(Modifier.height(6.dp))
    FlowFinHeroAmount(
      whole = state.amountWhole,
      currency = state.currency,
      decimal = state.amountDecimal.orEmpty(),
      tint = if (state.amount == null || !state.amount.isPositive) palette.textFaint else palette.text,
    )
    Spacer(Modifier.height(10.dp))
    Box(
      Modifier
        .width(168.dp)
        .height(if (state.amountFocused) 1.5.dp else 1.dp)
        .background(if (state.amountFocused) palette.accent else palette.border),
    )
  }
}

@Composable
private fun PickerLabel(text: String) {
  Text(
    text = text.uppercase(),
    modifier = Modifier.padding(bottom = 10.dp),
    style = FlowFinTheme.typography.label,
    color = FlowFinTheme.colors.textSoft,
  )
}

/**
 * The funding schedule. Its own card because it isn't a field — it's a second thing
 * the save creates, and the user should be able to see that at a glance.
 */
@Composable
private fun RefillCard(
  state: AddBudgetUiState,
  onToggleRefill: () -> Unit,
  onToggleFundNow: () -> Unit,
  onPickDay: () -> Unit,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .padding(horizontal = 16.dp, vertical = 14.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.add_budget_refill_title),
          style = FlowFinTheme.typography.bodyLg,
          color = palette.text,
        )
        Text(
          text = stringResource(R.string.add_budget_refill_sub).uppercase(),
          modifier = Modifier.padding(top = 3.dp),
          style = FlowFinTheme.typography.label.copy(fontSize = 10.sp),
          color = palette.textSoft,
        )
      }
      FlowFinSwitch(checked = state.refillMonthly, onCheckedChange = { onToggleRefill() })
    }

    if (state.refillMonthly) {
      Spacer(Modifier.height(6.dp))
      HorizontalDivider(color = palette.border)
      FlowFinFormRow(
        label = stringResource(R.string.add_budget_refill_day),
        value = stringResource(R.string.add_budget_day_of_month, state.refillDay),
        valueSub = state.nextRefill?.asString(),
        onClick = onPickDay,
      )
    }

    HorizontalDivider(color = palette.border)
    Row(
      modifier = Modifier.fillMaxWidth().clickable { onToggleFundNow() }.padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.add_budget_fund_now),
        modifier = Modifier.weight(1f),
        style = FlowFinTheme.typography.bodyLg,
        color = palette.text,
      )
      FlowFinSwitch(checked = state.fundNow, onCheckedChange = { onToggleFundNow() })
    }
  }
}

@Composable
private fun ParentSheet(
  state: AddBudgetUiState,
  onDismiss: () -> Unit,
  onSelect: (AccountId) -> Unit,
) {
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.add_budget_parent_label), onClose = onDismiss)
    Column(Modifier.padding(horizontal = 10.dp).padding(bottom = 16.dp)) {
      state.parentOptions.forEach { option ->
        FlowFinPickerRow(
          icon = categoryIcon(option.iconKey),
          name = option.name,
          selected = option.id == state.parent,
          amount = option.balance,
          tint = categoryColor(option.colorKey),
          onClick = { onSelect(option.id) },
        )
      }
    }
  }
}

@Composable
private fun RefillDaySheet(selected: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
  val palette = FlowFinTheme.colors
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(title = stringResource(R.string.add_budget_refill_day_sheet), onClose = onDismiss)
    LazyVerticalGrid(
      columns = GridCells.Fixed(7),
      modifier = Modifier.padding(horizontal = HORIZONTAL).padding(bottom = 20.dp).height(220.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items((1..31).toList()) { day ->
        val on = day == selected
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (on) palette.accent else palette.surface2)
            .clickable { onSelect(day) },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = day.toString(),
            style = FlowFinTheme.typography.body,
            color = if (on) palette.onAccent else palette.text,
          )
        }
      }
    }
  }
}

@Preview(name = "Add budget", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAddBudget() = FlowFinTheme {
  AddBudgetScreen(
    state = AddBudgetUiState(currency = "Rs", name = "Food", amountWhole = "28,000"),
    snackbarHostState = remember { SnackbarHostState() },
  )
}
