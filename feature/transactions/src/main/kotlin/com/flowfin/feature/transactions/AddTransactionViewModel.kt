package com.flowfin.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.MoneyFormatter
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

class AddTransactionViewModel(
  private val recordTransaction: RecordTransaction,
  accounts: AccountRepository,
  categories: CategoryRepository,
  private val money: MoneyFormatter,
  private val clock: Clock,
  private val zone: TimeZone,
) : ViewModel() {

  private val today: LocalDate = clock.now().toLocalDateTime(zone).date
  private val form = MutableStateFlow(AddTransactionUiState(date = today))

  private val effectChannel = Channel<AddTransactionEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  /** Merges the editable form with the live account/category options. */
  val uiState: StateFlow<AddTransactionUiState> = combine(
    form,
    accounts.observeBalances(),
    categories.observeAll(),
  ) { current, balances, categoryList ->
    current.copy(
      accountOptions = balances.map { it.toOption() },
      categoryOptions = categoryList.filter { it.scope == current.type.scope }.map { it.toOption() },
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), form.value)

  fun onKey(key: CalculatorKey) = form.update { it.withCalculator(it.calculator.press(key)) }

  fun onSelectType(type: EntryType) = form.update {
    it.copy(type = type, category = CategorySelection(null)) // category scope changes with type
  }

  fun onPickFromAccount(id: AccountId) = form.update { it.copy(fromAccount = AccountSelection(id, isPristine = false), openSheet = null) }
  fun onPickToAccount(id: AccountId) = form.update { it.copy(toAccount = AccountSelection(id, isPristine = false), openSheet = null) }
  fun onPickCategory(id: CategoryId) = form.update { it.copy(category = CategorySelection(id, isPristine = false), openSheet = null) }
  fun onPickDate(date: LocalDate) = form.update { it.copy(date = date, openSheet = null) }
  fun onNoteChange(text: String) = form.update { it.copy(note = text) }
  fun openSheet(sheet: AddSheet) = form.update { it.copy(openSheet = sheet) }
  fun dismissSheet() = form.update { it.copy(openSheet = null) }

  fun save() {
    val state = form.value
    if (!state.canSave) return
    val amount = state.calculator.settled().value ?: return
    val recordedAt = state.date.atStartOfDayIn(zone)
    val note = state.note.trim().ifBlank { null }
    val draft = when (state.type) {
      EntryType.Expense -> TransactionDraft.Expense(state.fromAccount.value ?: return, amount, state.category.value ?: return, note, recordedAt)
      EntryType.Income -> TransactionDraft.Income(state.toAccount.value ?: return, amount, state.category.value ?: return, note, recordedAt)
      EntryType.Transfer -> TransactionDraft.Transfer(state.fromAccount.value ?: return, state.toAccount.value ?: return, amount, note, recordedAt)
    }
    form.update { it.copy(submitting = true) }
    viewModelScope.launch {
      when (val result = recordTransaction(draft)) {
        is Either.Right -> effectChannel.send(AddTransactionEffect.NavigateBack)
        is Either.Left -> {
          form.update { it.copy(submitting = false) }
          effectChannel.send(AddTransactionEffect.ShowMessage(result.value.toMessage()))
        }
      }
    }
  }

  private fun AddTransactionUiState.withCalculator(calculator: CalculatorState): AddTransactionUiState = copy(
    calculator = calculator,
    amountWhole = money.group(calculator.wholeDigits.toLong()),
    amountDecimal = calculator.decimalPart,
    expression = calculator.expression { it.stripTrailingZeros().toPlainString() },
    amount = AmountInput(calculator.settled().value, isPristine = false),
  )

  private fun AccountBalance.toOption() = AccountOption(
    id = account.id,
    name = account.name,
    iconKey = account.icon ?: if (account.isBudget) "wallet" else "bank",
    colorKey = account.color ?: if (account.isBudget) "other" else "bank",
    balance = money.display(balance),
    isBudget = account.isBudget,
  )

  private fun Category.toOption() = CategoryOption(id, name, icon, color)
}

private const val STOP_TIMEOUT_MS = 5_000L
