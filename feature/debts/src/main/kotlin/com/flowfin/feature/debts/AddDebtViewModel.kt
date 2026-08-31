package com.flowfin.feature.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.domain.usecase.CreatePerson
import com.flowfin.core.domain.usecase.RecordBorrow
import com.flowfin.core.domain.usecase.RecordLend
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.Person
import com.flowfin.core.model.PersonId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.detailDateLabel
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
 * Drives the Add-Debt form. Direction picks the use case — [RecordBorrow] for
 * money I owe, [RecordLend] for money owed to me — and both accept a null
 * account, which is how an off-book debt is opened.
 *
 * A person typed but not picked is created on save, so recording a debt against
 * someone new is one flow rather than two.
 */
class AddDebtViewModel(
  private val recordBorrow: RecordBorrow,
  private val recordLend: RecordLend,
  private val createPerson: CreatePerson,
  private val persons: PersonRepository,
  accounts: AccountRepository,
  private val money: MoneyFormatter,
  private val clock: Clock,
  private val zone: TimeZone,
) : ViewModel() {

  // Sampled once so the date row stays stable for the screen's life.
  private val now = clock.now()

  private val form = MutableStateFlow(
    AddDebtUiState(dateLabel = detailDateLabel(now.toLocalDateTime(zone).date)),
  )

  private val effectChannel = Channel<AddDebtEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  /** Merges the editable form with the live contact/account options. */
  val uiState: StateFlow<AddDebtUiState> = combine(
    form,
    persons.observeActive(),
    accounts.observeBalances(),
  ) { current, people, balances ->
    current.copy(
      personOptions = people.map { it.toOption() },
      // Debt money only moves through real, active accounts — the borrow/lend
      // use cases reject anything else, so don't offer it.
      accountOptions = balances.filter { it.account.isReal && !it.account.isArchived }.map { it.toOption() },
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), form.value)

  fun onKey(key: CalculatorKey) = form.update { it.withCalculator(it.calculator.press(key)) }

  fun onSelectDirection(direction: DebtDirection) = form.update { it.copy(direction = direction) }

  fun onOpenSheet(sheet: AddDebtSheet) = form.update { it.copy(openSheet = sheet) }

  fun onDismissSheet() = form.update { it.copy(openSheet = null, personQuery = "") }

  fun onPersonQueryChange(query: String) = form.update { it.copy(personQuery = query) }

  fun onPickPerson(id: PersonId) = form.update {
    it.copy(person = id, newPersonName = "", personQuery = "", openSheet = null)
  }

  /** Accepts the typed name as a new contact — created for real on save. */
  fun onUseTypedPerson() = form.update {
    it.copy(person = null, newPersonName = it.personQuery.trim(), personQuery = "", openSheet = null)
  }

  fun onReasonChange(reason: String) = form.update { it.copy(reason = reason) }

  fun onLinkAccountChange(linked: Boolean) = form.update {
    it.copy(linkAccount = linked, account = if (linked) it.account else null)
  }

  fun onPickAccount(id: AccountId) = form.update {
    it.copy(account = id, linkAccount = true, openSheet = null)
  }

  fun save() {
    val state = uiState.value
    if (!state.canSave) return
    val amount = state.calculator.settled().value ?: return
    form.update { it.copy(submitting = true) }

    viewModelScope.launch {
      val personId = state.person ?: createContact(state.newPersonName)
      if (personId == null) {
        form.update { it.copy(submitting = false) }
        effectChannel.send(AddDebtEffect.ShowMessage(UiText.Res(R.string.add_debt_error)))
        return@launch
      }

      val account = state.account.takeIf { state.linkAccount }
      val result = if (state.isBorrowing) {
        recordBorrow(personId, account, amount, state.reason.trim().ifBlank { null }, now)
      } else {
        recordLend(personId, account, amount, state.reason.trim().ifBlank { null }, now)
      }

      when (result) {
        is Either.Right -> effectChannel.send(AddDebtEffect.Saved)
        is Either.Left -> {
          form.update { it.copy(submitting = false) }
          effectChannel.send(AddDebtEffect.ShowMessage(UiText.Res(R.string.add_debt_error)))
        }
      }
    }
  }

  /**
   * Resolves the typed name to a contact: an existing one if the name already
   * exists (create would be rejected as a duplicate), otherwise a new one.
   */
  private suspend fun createContact(name: String): PersonId? {
    persons.findByName(name.trim())?.let { return it.id }
    return when (val created = createPerson(name)) {
      is Either.Right -> created.value.id
      is Either.Left -> null
    }
  }

  private fun AddDebtUiState.withCalculator(calculator: CalculatorState): AddDebtUiState = copy(
    calculator = calculator,
    amountWhole = money.group(calculator.wholeDigits.toLong()),
    amountDecimal = calculator.decimalPart,
    expression = calculator.expression { it.stripTrailingZeros().toPlainString() },
    amount = calculator.settled().value,
  )

  private fun Person.toOption() = DebtPersonOption(id, name, avatarTintIndex)

  private fun AccountBalance.toOption() = DebtAccountOption(
    id = account.id,
    name = account.name,
    iconKey = account.icon,
    colorKey = account.color,
    balance = money.display(balance),
  )
}

private const val STOP_TIMEOUT_MS = 5_000L
