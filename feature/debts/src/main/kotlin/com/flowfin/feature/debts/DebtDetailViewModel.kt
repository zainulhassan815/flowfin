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
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Drives Debt detail: one debt, its person, and its movement timeline, all
 * observed so a repayment recorded on its own screen lands here without a reload.
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

  private val effectChannel = Channel<DebtDetailEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  val uiState: StateFlow<DebtDetailUiState> = combine(
    debts.observeWithRemaining(debtId),
    transactions.observeByDebt(debtId),
    persons.observeActive(),
  ) { debt, movements, people ->
    if (debt == null) return@combine DebtDetailUiState.NotFound
    debt.toContent(people.firstOrNull { it.id == debt.debt.personId }, movements)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DebtDetailUiState.Loading)

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

  private fun DebtWithRemaining.toContent(
    person: Person?,
    movements: List<Transaction>,
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
