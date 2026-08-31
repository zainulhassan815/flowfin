package com.flowfin.feature.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.domain.usecase.RecordRepayment
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.DebtWithRemaining
import com.flowfin.core.model.Money
import com.flowfin.core.model.Person
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.detailDateLabel
import com.flowfin.core.ui.monthShortLabel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Drives Debt detail: one debt, its person, and its movement timeline, all
 * observed so a recorded repayment lands on screen without a reload. The
 * record-payment sheet is held here too — it's a sub-state of this screen, not
 * a route, because it renders the debt's own remaining and person.
 *
 * Repayments go through [RecordRepayment] rather than the repository so the
 * account rules (real, active, matching currency) are enforced.
 */
class DebtDetailViewModel(
  private val debtId: DebtId,
  private val debts: DebtRepository,
  private val persons: PersonRepository,
  private val transactions: TransactionRepository,
  private val accounts: AccountRepository,
  private val recordRepayment: RecordRepayment,
  private val money: MoneyFormatter,
  private val clock: Clock = Clock.System,
) : ViewModel() {

  private val zone = TimeZone.currentSystemDefault()

  /** The sheet, or null when closed. Merged over the observed debt on every emission. */
  private val sheet = MutableStateFlow<RecordPaymentUi?>(null)

  private val effectChannel = Channel<DebtDetailEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  val uiState: StateFlow<DebtDetailUiState> = combine(
    debts.observeWithRemaining(debtId),
    transactions.observeByDebt(debtId),
    persons.observeActive(),
    accounts.observeBalances(),
    sheet,
  ) { debt, movements, people, balances, openSheet ->
    if (debt == null) return@combine DebtDetailUiState.NotFound
    val person = people.firstOrNull { it.id == debt.debt.personId }
    debt.toContent(person, movements, openSheet?.refreshed(debt, person, balances))
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DebtDetailUiState.Loading)

  // --- Sheet ---

  fun openSheet() {
    val content = uiState.value as? DebtDetailUiState.Content ?: return
    sheet.value = RecordPaymentUi(
      title = UiText.Res(R.string.debt_detail_sheet_title),
      amountLabel = UiText.Res(R.string.debt_detail_sheet_amount_label),
      amountDigits = "",
      amountWhole = "0",
      amountDecimal = ".00",
      personName = content.personName,
      avatarTintIndex = content.avatarTintIndex,
      reason = content.reason,
      remainingWhole = content.remainingWhole,
      remainingDecimal = content.remainingDecimal,
      remainingLabel = content.remainingLabel,
      afterRemaining = null,
      linkAccount = false,
      linkLabel = UiText.Res(R.string.debt_detail_sheet_link_label),
      linkDescription = UiText.Res(R.string.debt_detail_sheet_link_desc),
      accounts = emptyList(),
      selectedAccountId = null,
      dateLabel = detailDateLabel(clock.now().toLocalDateTime(zone).date),
      saveLabel = UiText.Res(R.string.debt_detail_sheet_save),
      note = "",
      saving = false,
    )
  }

  fun closeSheet() {
    sheet.value = null
  }

  fun onAmountDigits(digits: String) {
    // Digits only, and capped so the buffer can't outgrow what Money can hold.
    val cleaned = digits.filter { it.isDigit() }.trimStart('0').take(MAX_AMOUNT_DIGITS)
    sheet.update { it.copy(amountDigits = cleaned) }
  }

  fun onLinkAccountChange(linked: Boolean) {
    sheet.update { it.copy(linkAccount = linked, selectedAccountId = if (linked) it.selectedAccountId else null) }
  }

  fun onAccountSelected(id: AccountId) {
    sheet.update { it.copy(selectedAccountId = id, linkAccount = true) }
  }

  fun onNoteChange(note: String) {
    sheet.update { it.copy(note = note) }
  }

  fun save() {
    val form = sheet.value ?: return
    if (!form.canSave) return
    sheet.update { it.copy(saving = true) }
    viewModelScope.launch {
      val result = recordRepayment(
        debtId = debtId,
        account = form.selectedAccountId.takeIf { form.linkAccount },
        amount = Money(form.amountMinor),
        recordedAt = clock.now(),
        note = form.note.trim().ifBlank { null },
      )
      when (result) {
        is Either.Right -> sheet.value = null // the observed debt re-emits with the new remaining
        is Either.Left -> {
          sheet.update { it.copy(saving = false) }
          effectChannel.send(DebtDetailEffect.ShowMessage(UiText.Res(R.string.debt_action_error)))
        }
      }
    }
  }

  // --- Debt actions ---

  fun toggleSettled() {
    val content = uiState.value as? DebtDetailUiState.Content ?: return
    viewModelScope.launch {
      val result = if (content.isSettled) debts.reopen(debtId) else debts.markSettled(debtId)
      if (result is Either.Left) {
        effectChannel.send(DebtDetailEffect.ShowMessage(UiText.Res(R.string.debt_action_error)))
      }
    }
  }

  fun delete() {
    viewModelScope.launch {
      val effect = when (debts.delete(debtId)) {
        is Either.Right -> DebtDetailEffect.Dismiss
        is Either.Left -> DebtDetailEffect.ShowMessage(UiText.Res(R.string.debt_action_error))
      }
      effectChannel.send(effect)
    }
  }

  // --- Mapping ---

  private fun MutableStateFlow<RecordPaymentUi?>.update(block: (RecordPaymentUi) -> RecordPaymentUi) {
    value = value?.let(block)
  }

  /**
   * Re-derives the sheet's read-only halves — the debt's live remaining, the
   * account list, and the "after this" preview — from the latest emission, so
   * the sheet stays truthful while it's open.
   */
  private fun RecordPaymentUi.refreshed(
    debt: DebtWithRemaining,
    person: Person?,
    balances: List<AccountBalance>,
  ): RecordPaymentUi {
    val amount = Money(amountMinor)
    val after = debt.remaining - amount
    val owing = debt.debt.direction == DebtDirection.I_OWE
    return copy(
      title = UiText.Res(if (owing) R.string.debt_detail_sheet_title else R.string.debt_detail_sheet_title_receipt),
      amountLabel = UiText.Res(
        if (owing) R.string.debt_detail_sheet_amount_label else R.string.debt_detail_sheet_amount_label_receipt,
      ),
      amountWhole = money.group(amountMinor / 100),
      amountDecimal = "." + (amountMinor % 100).toString().padStart(2, '0'),
      personName = person?.name.orEmpty(),
      avatarTintIndex = person?.avatarTintIndex ?: 1,
      reason = debt.debt.reason,
      remainingWhole = money.whole(debt.remaining),
      remainingDecimal = money.fraction(debt.remaining),
      remainingLabel = UiText.Res(if (owing) R.string.debts_amount_label_i_owe else R.string.debts_amount_label_owe_me),
      afterRemaining = when {
        amountMinor == 0L -> null
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
      // Debt money only moves through real, active accounts (RecordRepayment
      // enforces it) — so only offer those.
      accounts = balances
        .filter { it.account.isReal && !it.account.isArchived && it.account.currency == debt.debt.currency }
        .map {
          RepaymentAccountUi(
            id = it.account.id,
            name = it.account.name,
            iconKey = it.account.icon,
            colorKey = it.account.color,
            balance = money.display(it.balance),
          )
        },
    )
  }

  private fun DebtWithRemaining.toContent(
    person: Person?,
    movements: List<Transaction>,
    openSheet: RecordPaymentUi?,
  ): DebtDetailUiState.Content {
    val owing = debt.direction == DebtDirection.I_OWE
    val original = debt.originalAmount.minorUnits
    val percent = if (original > 0) ((paid.minorUnits * 100) / original).toInt().coerceIn(0, 100) else 0
    val personName = person?.name.orEmpty()
    return DebtDetailUiState.Content(
      personName = personName,
      avatarTintIndex = person?.avatarTintIndex ?: 1,
      reason = debt.reason,
      openedLabel = openedLabel(originRecordedAt.toLocalDateTime(zone).date),
      isSettled = debt.isSettled,
      directionLabel = UiText.Res(if (owing) R.string.debts_tab_i_owe else R.string.debts_tab_owe_me),
      remainingLabel = UiText.Res(if (owing) R.string.debt_detail_remaining_i_owe else R.string.debt_detail_remaining_owe_me),
      remainingWhole = money.whole(remaining),
      remainingDecimal = money.fraction(remaining),
      originalAmount = money.display(debt.originalAmount),
      paidAmount = money.display(paid),
      remainingAmount = money.display(remaining),
      paidPercent = percent,
      progress = if (original > 0) (paid.minorUnits.toFloat() / original).coerceIn(0f, 1f) else 0f,
      isFullyPaid = !remaining.isPositive,
      timeline = movements.sortedByDescending { it.recordedAt }.map { it.toTimelineItem(personName, owing) },
      paymentCount = movements.count { it.kind.isRepayment },
      sheet = openSheet,
    )
  }

  private fun Transaction.toTimelineItem(personName: String, owing: Boolean): DebtTimelineItemUi {
    val date = recordedAt.toLocalDateTime(zone).date
    val isOrigin = !kind.isRepayment
    val titleRes = when {
      isOrigin && owing -> R.string.debt_detail_timeline_borrowed
      isOrigin -> R.string.debt_detail_timeline_lent
      owing -> R.string.debt_detail_timeline_payment_to
      else -> R.string.debt_detail_timeline_receipt_from
    }
    return DebtTimelineItemUi(
      id = id.value.toString(),
      dateLabel = detailDateLabel(date),
      title = UiText.Res(titleRes, listOf(personName)),
      meta = when {
        !note.isNullOrBlank() -> UiText.Raw(note!!)
        isOrigin -> UiText.Res(R.string.debt_detail_timeline_original)
        else -> null
      },
      // The origin adds to what's outstanding, a repayment subtracts from it —
      // the sign reads against the debt, not against any account.
      amount = (if (isOrigin) "+" else "−") + money.whole(amount),
      decimal = money.fraction(amount),
      isOrigin = isOrigin,
    )
  }

  private fun openedLabel(date: LocalDate): UiText {
    val today = clock.now().toLocalDateTime(zone).date
    val days = today.toEpochDays() - date.toEpochDays()
    val relative = if (days <= 0) UiText.Res(R.string.home_days_ago_today) else UiText.Plural(R.plurals.home_days_ago, days)
    return UiText.Res(R.string.debts_card_date, listOf(date.dayOfMonth, UiText.Raw(monthShortLabel(date)), relative))
  }
}

private val TransactionKind.isRepayment: Boolean
  get() = this == TransactionKind.DEBT_REPAY_OUT || this == TransactionKind.DEBT_REPAY_IN

private const val STOP_TIMEOUT_MS = 5_000L
private const val MAX_AMOUNT_DIGITS = 12
