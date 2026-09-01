package com.flowfin.feature.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.domain.usecase.RecordRepayment
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Money
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.colorKey
import com.flowfin.core.ui.detailDateLabel
import com.flowfin.core.ui.iconKey
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
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Drives the Record-payment screen for one debt.
 *
 * The debt is observed rather than snapshotted, so the remaining it shows — and the
 * "after this" preview beside it — stay honest if anything else moves the debt while
 * the form is open. Repayments go through [RecordRepayment] so the account rules
 * (real, active, matching currency) are enforced.
 */
class RecordPaymentViewModel(
  private val debtId: DebtId,
  debts: DebtRepository,
  persons: PersonRepository,
  accounts: AccountRepository,
  private val recordRepayment: RecordRepayment,
  private val money: MoneyFormatter,
  private val clock: Clock,
  private val zone: TimeZone,
) : ViewModel() {

  private val form = MutableStateFlow(
    RecordPaymentUiState(currency = money.symbol, date = today(), dateLabel = detailDateLabel(today())),
  )

  private val effectChannel = Channel<RecordPaymentEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  val uiState: StateFlow<RecordPaymentUiState> = combine(
    form,
    debts.observeWithRemaining(debtId),
    persons.observeActive(),
    accounts.observeBalances(),
  ) { current, debt, people, balances ->
    if (debt == null) return@combine current.copy(loading = false, notFound = true)
    val person = people.firstOrNull { it.id == debt.debt.personId }
    val owing = debt.debt.direction == DebtDirection.I_OWE
    val amount = current.amount ?: Money.ZERO
    val after = debt.remaining - amount

    current.copy(
      loading = false,
      title = UiText.Res(if (owing) R.string.debt_detail_sheet_title else R.string.debt_detail_sheet_title_receipt),
      amountLabel = UiText.Res(
        if (owing) R.string.debt_detail_sheet_amount_label else R.string.debt_detail_sheet_amount_label_receipt,
      ),
      personName = person?.name.orEmpty(),
      avatarTintIndex = person?.avatarTintIndex ?: 1,
      reason = debt.debt.reason,
      remainingWhole = money.whole(debt.remaining),
      remainingDecimal = money.fraction(debt.remaining),
      remainingLabel = UiText.Res(if (owing) R.string.debts_amount_label_i_owe else R.string.debts_amount_label_owe_me),
      afterRemaining = when {
        amount.isZero -> null
        // Past the remaining is allowed — forgiveness / settling up — so say so
        // plainly rather than blocking the save.
        after.isNegative -> UiText.Res(R.string.debt_detail_sheet_after_overpaid, listOf(money.display(-after)))
        else -> UiText.Res(
          if (owing) R.string.debt_detail_sheet_after_i_owe else R.string.debt_detail_sheet_after_owe_me,
          listOf(money.display(after)),
        )
      },
      linkLabel = UiText.Res(if (owing) R.string.debt_detail_sheet_link_label else R.string.debt_detail_sheet_link_label_receipt),
      linkDescription = UiText.Res(
        if (owing) R.string.debt_detail_sheet_link_desc else R.string.debt_detail_sheet_link_desc_receipt,
      ),
      saveLabel = UiText.Res(if (owing) R.string.debt_detail_sheet_save else R.string.debt_detail_sheet_save_receipt),
      accounts = balances
        .filter { it.account.isReal && !it.account.isArchived }
        .map {
          RepaymentAccountUi(
            id = it.account.id,
            name = it.account.name,
            iconKey = it.account.iconKey(),
            colorKey = it.account.colorKey(),
            balance = money.displayWhole(it.balance),
          )
        },
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), form.value)

  fun onKey(key: CalculatorKey) = form.update { it.withCalculator(it.calculator.press(key)) }

  /** Focusing the amount raises the keypad; anything else taking focus drops it. */
  fun onFocusAmount() = form.update { it.copy(amountFocused = true, openSheet = null) }

  fun onBlurAmount() = form.update { it.copy(amountFocused = false) }

  fun onLinkAccountChange(linked: Boolean) = form.update {
    it.copy(linkAccount = linked, selectedAccountId = if (linked) it.selectedAccountId else null)
  }

  fun onAccountSelected(id: AccountId) = form.update {
    it.copy(selectedAccountId = id, linkAccount = true)
  }

  fun onNoteChange(note: String) = form.update { it.copy(note = note) }

  fun onOpenSheet(sheet: RecordPaymentSheet) = form.update { it.copy(openSheet = sheet, amountFocused = false) }

  fun onDismissSheet() = form.update { it.copy(openSheet = null) }

  fun onPickDate(date: LocalDate) = form.update {
    it.copy(date = date, dateLabel = detailDateLabel(date), openSheet = null)
  }

  fun save() {
    val state = form.value
    val amount = state.calculator.settled().value
    if (amount == null || !amount.isPositive || state.saving) return
    if (state.linkAccount && state.selectedAccountId == null) return
    form.update { it.copy(saving = true) }

    viewModelScope.launch {
      val result = recordRepayment(
        debtId = debtId,
        account = state.selectedAccountId.takeIf { state.linkAccount },
        amount = amount,
        // Midday, so a backdated repayment can't land on the wrong side of a
        // timezone boundary and read as the day before.
        recordedAt = (state.date ?: today()).atTime(12, 0).toInstant(zone),
        note = state.note.trim().ifBlank { null },
      )
      when (result) {
        is Either.Right -> effectChannel.send(RecordPaymentEffect.NavigateBack)
        is Either.Left -> {
          form.update { it.copy(saving = false) }
          effectChannel.send(RecordPaymentEffect.ShowMessage(UiText.Res(R.string.debt_action_error)))
        }
      }
    }
  }

  private fun RecordPaymentUiState.withCalculator(next: CalculatorState) = copy(
    calculator = next,
    amountWhole = money.group(next.wholeDigits.toLong()),
    amountDecimal = next.decimalPart,
    expression = next.expression { money.group(it.toLong()) },
    amount = next.settled().value,
  )

  private fun today(): LocalDate = clock.now().toLocalDateTime(zone).date
}

/** One-shot outcomes of the Record-payment screen. */
sealed interface RecordPaymentEffect {
  data object NavigateBack : RecordPaymentEffect
  data class ShowMessage(val text: UiText) : RecordPaymentEffect
}

private const val STOP_TIMEOUT_MS = 5_000L
