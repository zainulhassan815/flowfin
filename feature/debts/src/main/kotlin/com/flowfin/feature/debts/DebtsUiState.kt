package com.flowfin.feature.debts

import com.flowfin.core.model.DebtId
import com.flowfin.core.ui.UiText

/**
 * The Debts tab: a net position headline over two direction tabs (I Owe /
 * Owe Me), each an active list plus a collapsible settled list. Read-only for
 * now — detail, add-debt, and record-payment/receipt land in later slices.
 */
sealed interface DebtsUiState {
  data object Loading : DebtsUiState

  /** No debts at all, either direction. */
  data object Empty : DebtsUiState

  data class Content(
    val netPositionWhole: String,
    val netPositionDecimal: String,
    /** Every debt in the app is settled — the hero reads "All clear." instead of an amount. */
    val allSettled: Boolean,
    val iOwe: DebtsTabUi,
    val oweMe: DebtsTabUi,
  ) : DebtsUiState
}

/** One direction's tab content — active debts, plus settled ones behind a disclosure. */
data class DebtsTabUi(
  val active: List<DebtCardUi>,
  val settled: List<DebtCardUi>,
)

/** A debt card — the same shape for active and settled; the screen renders each list differently. */
data class DebtCardUi(
  val id: DebtId,
  val personName: String,
  val avatarTintIndex: Int,
  val reason: String?,
  val amountWhole: String,
  val amountDecimal: String,
  val paidWhole: String,
  val paidDecimal: String,
  val progress: Float,
  val dateLabel: UiText,
)
