package com.flowfin.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.usecase.CreateRecurringSchedule
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringDraft
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

class AddRecurringViewModel(
  private val createSchedule: CreateRecurringSchedule,
  accounts: AccountRepository,
  categories: CategoryRepository,
  private val money: MoneyFormatter,
  clock: Clock,
  private val zone: TimeZone,
) : ViewModel() {

  // Sampled once so the "Next: …" preview stays stable for the screen's life.
  private val now = clock.now()
  private val today: LocalDate = now.toLocalDateTime(zone).date

  private val form = MutableStateFlow(
    AddRecurringUiState(
      weeklyDay = today.dayOfWeek.isoDayNumber,
      monthlyDay = today.dayOfMonth,
      yearlyMonth = today.monthNumber,
      yearlyDay = today.dayOfMonth,
    ),
  )

  private val effectChannel = Channel<AddRecurringEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  /** Merges the editable form with the live account/category options. */
  val uiState: StateFlow<AddRecurringUiState> = combine(
    form,
    accounts.observeBalances(),
    categories.observeAll(),
  ) { current, balances, categoryList ->
    current.withCadenceLabels().copy(
      accountOptions = balances.map { it.toOption() },
      categoryOptions = categoryList.filter { it.scope == current.type.scope }.map { it.toOption() },
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), form.value.withCadenceLabels())

  fun onKey(key: CalculatorKey) = form.update { it.withCalculator(it.calculator.press(key)) }

  /** Focusing the amount raises the keypad; anything else taking focus drops it. */
  fun onFocusAmount() = form.update { it.copy(amountFocused = true, openSheet = null) }

  fun onBlurAmount() = form.update { it.copy(amountFocused = false) }

  fun onNameChange(text: String) = form.update { it.copy(name = text) }

  fun onSelectType(type: RecurringEntryType) {
    // Income can't fire into a budget envelope, so a budget pick doesn't carry over.
    val selectedIsBudget = uiState.value.selectedAccount()?.isBudget == true
    form.update {
      it.copy(
        type = type,
        category = null, // category scope changes with type
        account = if (type == RecurringEntryType.Income && selectedIsBudget) null else it.account,
      )
    }
  }

  fun onSelectFrequency(frequency: RecurringFrequency) = form.update { it.copy(frequency = frequency) }

  fun onPickWeekday(isoDayNumber: Int) = form.update { it.copy(weeklyDay = isoDayNumber, openSheet = null) }
  fun onPickMonthDay(day: Int) = form.update { it.copy(monthlyDay = day, openSheet = null) }

  /** Month keeps the sheet open — the day pick below it closes the exchange. */
  fun onPickYearlyMonth(month: Int) = form.update { it.copy(yearlyMonth = month) }
  fun onPickYearlyDay(day: Int) = form.update { it.copy(yearlyDay = day, openSheet = null) }

  fun onPickAccount(id: AccountId) = form.update { it.copy(account = id, openSheet = null) }
  fun onPickCategory(id: CategoryId) = form.update { it.copy(category = id, openSheet = null) }
  fun onNoteChange(text: String) = form.update { it.copy(note = text) }
  fun openSheet(sheet: AddRecurringSheet) = form.update { it.copy(openSheet = sheet, amountFocused = false) }
  fun dismissSheet() = form.update { it.copy(openSheet = null) }

  fun save() {
    val state = uiState.value
    if (!state.canSave) return
    val amount = state.calculator.settled().value ?: return
    val account = state.account ?: return
    val category = state.category ?: return
    val draft = when (state.type) {
      RecurringEntryType.Expense -> RecurringDraft.Expense(state.name.trim(), amount, state.recurrence, account, category)
      RecurringEntryType.Income -> RecurringDraft.Income(state.name.trim(), amount, state.recurrence, account, category)
    }
    form.update { it.copy(submitting = true) }
    viewModelScope.launch {
      when (val result = createSchedule(draft)) {
        is Either.Right -> effectChannel.send(AddRecurringEffect.NavigateBack)
        is Either.Left -> {
          form.update { it.copy(submitting = false) }
          effectChannel.send(AddRecurringEffect.ShowMessage(result.value.toMessage()))
        }
      }
    }
  }

  private fun RecurringError.toMessage(): UiText = UiText.Res(
    when (this) {
      RecurringError.NameBlank -> R.string.add_recurring_error_name
      RecurringError.AmountNotPositive -> R.string.add_tx_error_amount
      is RecurringError.AccountNotFound -> R.string.add_tx_error_account_not_found
      is RecurringError.ArchivedAccount -> R.string.add_tx_error_account_archived
      is RecurringError.InvalidAccountForKind -> R.string.add_recurring_error_income_account
      is RecurringError.CategoryNotFound -> R.string.add_tx_error_category_not_found
      RecurringError.CategoryScopeMismatch -> R.string.add_tx_error_category_scope
      is RecurringError.ScheduleNotFound, is RecurringError.Unexpected -> R.string.add_tx_error_generic
    },
  )

  private fun AddRecurringUiState.withCadenceLabels(): AddRecurringUiState =
    copy(repeatsOn = recurrence.repeatsOnLabel(), nextDue = recurrence.nextDueLabel())

  private fun Recurrence.repeatsOnLabel(): UiText = when (this) {
    is Recurrence.Weekly -> UiText.Res(R.string.add_recurring_repeats_weekly, listOf(UiText.Raw(weekdayFull(dayOfWeek))))
    is Recurrence.Monthly -> UiText.Res(R.string.add_recurring_repeats_monthly, listOf(UiText.Raw(ordinal(dayOfMonth))))
    is Recurrence.Yearly -> UiText.Res(R.string.add_recurring_repeats_yearly, listOf(dayOfMonth, UiText.Raw(monthShort(Month(month)))))
  }

  private fun Recurrence.nextDueLabel(): UiText {
    val next = nextDueAfter(now, zone).toLocalDateTime(zone).date
    val inDays = next.toEpochDays() - today.toEpochDays()
    return UiText.Plural(
      R.plurals.add_recurring_next,
      inDays,
      listOf(next.dayOfMonth, UiText.Raw(monthShort(next.month)), next.year, inDays),
    )
  }

  private fun AddRecurringUiState.withCalculator(calculator: CalculatorState): AddRecurringUiState = copy(
    calculator = calculator,
    amountWhole = money.group(calculator.wholeDigits.toLong()),
    amountDecimal = calculator.decimalPart,
    expression = calculator.expression { it.stripTrailingZeros().toPlainString() },
    amount = calculator.settled().value,
  )

  private fun AccountBalance.toOption() = RecurringAccountOption(
    id = account.id,
    name = account.name,
    iconKey = account.icon ?: if (account.isBudget) "wallet" else "bank",
    colorKey = account.color ?: if (account.isBudget) "other" else "bank",
    balance = money.display(balance),
    isBudget = account.isBudget,
  )

  private fun Category.toOption() = RecurringCategoryOption(id, name, icon, color)
}

private const val STOP_TIMEOUT_MS = 5_000L
