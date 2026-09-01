package com.flowfin.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateRecurringSchedule
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringDraft
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.colorKey
import com.flowfin.core.ui.iconKey
import com.flowfin.core.ui.monthShortLabel
import com.flowfin.core.ui.press
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Drives the Add-Budget form — the screen that was missing entirely, leaving
 * [CreateBudget] reachable only from devtools.
 *
 * Saving does up to three things, in order, because a budget nobody funds is an
 * empty envelope: create it, attach its monthly funding schedule, and optionally
 * fund it once immediately. The budget is the only one of the three that can't be
 * skipped — if a follow-up fails the budget still exists, and the user is told.
 */
class AddBudgetViewModel(
  private val createBudget: CreateBudget,
  private val createRecurringSchedule: CreateRecurringSchedule,
  private val recordTransaction: RecordTransaction,
  accounts: AccountRepository,
  private val money: MoneyFormatter,
  private val clock: Clock,
  private val zone: TimeZone,
) : ViewModel() {

  private val form = MutableStateFlow(AddBudgetUiState(currency = money.symbol))

  private val effectChannel = Channel<AddBudgetEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  val uiState: StateFlow<AddBudgetUiState> = combine(
    form,
    accounts.observeBalances(),
  ) { current, balances ->
    val options = balances
      .filter { it.account.isReal && !it.account.isArchived }
      .map { BudgetParentOption(it.account.id, it.account.name, it.account.iconKey(), it.account.colorKey(), money.displayWhole(it.balance)) }
    // Skip the live duplicate check while submitting: the budget we just created
    // lands in the list before navigation finishes, flashing its own name as taken.
    val nameError = if (current.submitting) null else nameErrorFor(current.name, balances.map { it.account })
    current.copy(
      parentOptions = options,
      // One real account is not a choice — preselect it so the form opens ready.
      parent = current.parent ?: options.singleOrNull()?.id,
      nameError = nameError,
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), form.value.withNextRefill())

  fun onNameChange(text: String) = form.update { it.copy(name = text) }

  fun onSelectIcon(key: String) = form.update { it.copy(iconKey = key) }

  fun onSelectColor(key: String) = form.update { it.copy(colorKey = key) }

  fun onSelectParent(id: AccountId) = form.update { it.copy(parent = id, openSheet = null) }

  fun onToggleRefill() = form.update { it.copy(refillMonthly = !it.refillMonthly).withNextRefill() }

  fun onSelectRefillDay(day: Int) = form.update { it.copy(refillDay = day, openSheet = null).withNextRefill() }

  fun onToggleFundNow() = form.update { it.copy(fundNow = !it.fundNow) }

  fun onOpenSheet(sheet: AddBudgetSheet) = form.update { it.copy(openSheet = sheet, amountFocused = false) }

  fun onDismissSheet() = form.update { it.copy(openSheet = null) }

  /** Focusing the amount is what raises the keypad — see `FlowFinFormDock`. */
  fun onFocusAmount() = form.update { it.copy(amountFocused = true, openSheet = null) }

  fun onBlurAmount() = form.update { it.copy(amountFocused = false) }

  fun onKey(key: CalculatorKey) = form.update { it.withCalculator(it.calculator.press(key)) }

  fun save() {
    // Gate on the raw form, not on `canSave`: `nameError` is derived in the uiState
    // combine and never written back here, so reading it off `form` would leave Save
    // permanently blocked. CreateBudget remains the authority on duplicate names.
    val state = form.value
    val parent = state.parent
    val amount = state.calculator.settled().value
    if (state.name.isBlank() || parent == null || amount == null || !amount.isPositive || state.submitting) return
    form.update { it.copy(submitting = true) }

    viewModelScope.launch {
      val budget = createBudget(
        name = state.name.trim(),
        parentAccountId = parent,
        color = state.colorKey,
        icon = state.iconKey,
      )
      when (budget) {
        is Either.Left -> {
          form.update { it.copy(submitting = false) }
          effectChannel.send(AddBudgetEffect.ShowMessage(UiText.Res(R.string.add_budget_error_generic)))
          return@launch
        }

        is Either.Right -> {
          // The envelope exists from here on. The schedule and the first funding are
          // additive: if either fails the budget is still real, so we say so rather
          // than pretending the whole save failed.
          if (state.refillMonthly) {
            createRecurringSchedule(
              RecurringDraft.Allocation(
                name = state.name.trim(),
                amount = amount,
                recurrence = Recurrence.Monthly(state.refillDay),
                fromAccount = parent,
                toBudget = budget.value.id,
              ),
            )
          }
          if (state.fundNow) {
            recordTransaction(TransactionDraft.Allocation(parent, budget.value.id, amount, clock.now()))
          }
          effectChannel.send(AddBudgetEffect.NavigateBack)
        }
      }
    }
  }

  private fun nameErrorFor(name: String, accounts: List<Account>): NameError? {
    val trimmed = name.trim()
    return when {
      trimmed.isEmpty() -> NameError.Blank
      accounts.any { !it.isArchived && it.name == trimmed } -> NameError.Taken
      else -> null
    }
  }

  private fun AddBudgetUiState.withCalculator(next: CalculatorState): AddBudgetUiState = copy(
    calculator = next,
    amountWhole = money.group(next.wholeDigits.toLong()),
    amountDecimal = next.decimalPart,
    expression = next.expression { money.group(it.toLong()) },
    amount = next.settled().value,
  )

  /** The consequence of the chosen day, shown under it — "1 Oct 2026 · in 30 days". */
  private fun AddBudgetUiState.withNextRefill(): AddBudgetUiState {
    if (!refillMonthly) return copy(nextRefill = null)
    val now = clock.now()
    val next = Recurrence.Monthly(refillDay).nextDueAfter(now, zone).toLocalDateTime(zone).date
    val today = now.toLocalDateTime(zone).date
    val inDays = next.toEpochDays() - today.toEpochDays()
    return copy(
      nextRefill = UiText.Plural(
        R.plurals.recurring_due_in,
        inDays,
        listOf(next.dayOfMonth, UiText.Raw(monthShortLabel(next)), inDays),
      ),
    )
  }
}

private const val STOP_TIMEOUT_MS = 5_000L
