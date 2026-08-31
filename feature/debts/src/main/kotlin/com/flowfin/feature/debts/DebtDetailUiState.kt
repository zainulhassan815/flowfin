package com.flowfin.feature.debts

import com.flowfin.core.model.AccountId
import com.flowfin.core.ui.UiText

/**
 * Debt detail: who, how much is left, and every movement that got it there.
 * The record-payment sheet is part of this screen rather than its own route —
 * it needs the debt's remaining and person to render its context card, and it
 * dismisses back to here.
 */
sealed interface DebtDetailUiState {
  data object Loading : DebtDetailUiState

  /** Deleted, or an id that never existed. */
  data object NotFound : DebtDetailUiState

  data class Content(
    val personName: String,
    val avatarTintIndex: Int,
    val reason: String?,
    val openedLabel: UiText,
    val isSettled: Boolean,
    /** "I Owe" / "Owe Me" — the header pill. */
    val directionLabel: UiText,
    /** "You still owe" / "Still owed to you". */
    val remainingLabel: UiText,
    val remainingWhole: String,
    val remainingDecimal: String,
    val originalAmount: String,
    val paidAmount: String,
    val paidPercent: Int,
    val progress: Float,
    /** True once repayments cover the original — the hero reads as cleared. */
    val isFullyPaid: Boolean,
    val timeline: List<DebtTimelineItemUi>,
    val paymentCount: Int,
    val sheet: RecordPaymentUi?,
  ) : DebtDetailUiState
}

/** One movement on the debt: the origin borrow/lend, or a repayment. */
data class DebtTimelineItemUi(
  val id: String,
  val dateLabel: UiText,
  val title: UiText,
  val meta: UiText?,
  val amount: String,
  val decimal: String,
  val isOrigin: Boolean,
)

/**
 * The record-payment sheet's state. [amountDigits] is the raw digit buffer —
 * money is typed right-to-left into minor units, so there's no decimal point to
 * parse and no half-entered value to validate. [afterRemaining] previews where
 * the debt lands if this payment is saved.
 */
data class RecordPaymentUi(
  val title: UiText,
  val amountLabel: UiText,
  val amountDigits: String,
  val amountWhole: String,
  val amountDecimal: String,
  val personName: String,
  val avatarTintIndex: Int,
  val reason: String?,
  val remainingWhole: String,
  val remainingDecimal: String,
  val remainingLabel: UiText,
  val afterRemaining: UiText?,
  /** Off when the repayment is off-book — no account movement is recorded. */
  val linkAccount: Boolean,
  val linkLabel: UiText,
  val linkDescription: UiText,
  val accounts: List<RepaymentAccountUi>,
  val selectedAccountId: AccountId?,
  val dateLabel: UiText,
  val saveLabel: UiText,
  val note: String,
  val saving: Boolean,
) {
  val amountMinor: Long get() = amountDigits.toLongOrNull() ?: 0L

  /** A zero amount can't be saved; an account must be picked when linking is on. */
  val canSave: Boolean
    get() = !saving && amountMinor > 0 && (!linkAccount || selectedAccountId != null)
}

/** A real, active account the repayment can move through. */
data class RepaymentAccountUi(
  val id: AccountId,
  val name: String,
  val iconKey: String?,
  val colorKey: String?,
  val balance: String,
)
