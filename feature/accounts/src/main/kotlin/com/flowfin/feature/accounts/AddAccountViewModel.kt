package com.flowfin.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.model.Account
import com.flowfin.core.model.Money
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.ui.press
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the Add-Account form. The name's uniqueness is checked live against the
 * active accounts (the same rule `CreateRealAccount` enforces, surfaced before save
 * so the duplicate shows inline). Save maps the chosen [AccountKind] to icon/colour
 * and delegates to [CreateRealAccount].
 */
class AddAccountViewModel(
  private val createRealAccount: CreateRealAccount,
  accounts: AccountRepository,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val form = MutableStateFlow(AddAccountUiState(currency = money.symbol))

  private val effectChannel = Channel<AddAccountEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  val uiState: StateFlow<AddAccountUiState> = combine(
    form,
    accounts.observeActiveAccounts(),
  ) { current, active ->
    // While submitting, skip the live check: the just-created account lands in
    // [active] before navigation finishes, which would briefly flash the name the
    // user typed as a duplicate on the way out.
    val nameError = if (current.submitting) null else nameErrorFor(current.name, active)
    current.copy(nameError = nameError)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), form.value)

  fun onNameChange(text: String) = form.update { it.copy(name = text) }

  fun onSelectKind(kind: AccountKind) = form.update { it.copy(kind = kind) }

  fun onSelectColor(key: String) = form.update { it.copy(colorKey = key) }

  /** The opening balance is entered on the keypad now, like every other amount. */
  fun onKey(key: CalculatorKey) = form.update { current ->
    val next = current.calculator.press(key)
    current.copy(
      calculator = next,
      amountWhole = money.group(next.wholeDigits.toLong()),
      amountDecimal = next.decimalPart,
      expression = next.expression { money.group(it.toLong()) },
      openingBalance = next.settled().value,
    )
  }

  fun onFocusAmount() = form.update { it.copy(amountFocused = true) }

  fun onBlurAmount() = form.update { it.copy(amountFocused = false) }

  fun save() {
    // Gate on the form itself (the live duplicate check only runs while the screen
    // collects uiState); CreateRealAccount is the authority on uniqueness.
    val state = form.value
    if (state.name.isBlank() || state.submitting) return
    form.update { it.copy(submitting = true) }
    viewModelScope.launch {
      val result = createRealAccount(
        name = state.name.trim(),
        openingBalance = state.calculator.settled().value ?: Money.ZERO,
        color = state.colorKey ?: state.kind.colorKey,
        icon = state.kind.iconKey,
      )
      when (result) {
        is Either.Right -> effectChannel.send(AddAccountEffect.NavigateBack)
        is Either.Left -> {
          form.update { it.copy(submitting = false) }
          effectChannel.send(AddAccountEffect.ShowMessage(result.value.toMessage()))
        }
      }
    }
  }

  /** Blank → disabled (no error shown); a name already in use → inline [NameError.Taken]. */
  private fun nameErrorFor(name: String, active: List<Account>): NameError? {
    val trimmed = name.trim()
    return when {
      trimmed.isEmpty() -> NameError.Blank
      active.any { it.name == trimmed } -> NameError.Taken // case-sensitive, like the DB index
      else -> null
    }
  }
}

/**
 * Keep only digits and a single decimal point, capped at two fractional places.
 * Separators (spaces, commas) are dropped; a stray second dot ends the number.
 */
internal fun sanitizeAmount(text: String): String = buildString {
  var seenDot = false
  var decimals = 0
  for (c in text) {
    when {
      c == '.' -> { if (seenDot) break; seenDot = true; append('.') }
      c.isDigit() -> when {
        !seenDot -> append(c)
        decimals < 2 -> { decimals++; append(c) }
        else -> break
      }
    }
  }
}

/** Parse the major-unit input to [Money] (minor units); blank or partial → zero. */
internal fun parseAmount(text: String): Money {
  val parsed = text.toBigDecimalOrNull() ?: return Money.ZERO
  return Money(parsed.movePointRight(2).toLong())
}

private const val STOP_TIMEOUT_MS = 5_000L
