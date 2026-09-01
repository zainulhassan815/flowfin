package com.flowfin.feature.debts

import com.flowfin.core.model.AccountId
import com.flowfin.core.ui.UiText
import kotlinx.datetime.LocalDate

/**
 * Debt detail: who, how much is left, and every movement that got it there.
 * Recording a payment is its own route ([com.flowfin.core.navigation.RecordPaymentRoute]).
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
    /** Remaining in full display form — the hero renders its own currency mark,
     *  the stat row beside Original and Paid does not. */
    val remainingAmount: String,
    val paidPercent: Int,
    val progress: Float,
    /** True once repayments cover the original — the hero reads as cleared. */
    val isFullyPaid: Boolean,
    val timeline: List<DebtTimelineItemUi>,
    val paymentCount: Int,
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

/** A real, active account the repayment can move through. */
data class RepaymentAccountUi(
  val id: AccountId,
  val name: String,
  val iconKey: String?,
  val colorKey: String?,
  val balance: String,
)
