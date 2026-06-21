package com.flowfin.feature.transactions

import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.ui.UiText

/**
 * Transaction detail: the hero (kind, amount, note, date), the money-path rail
 * (from → to), and a delete action. Everything shown is a stable fact about the
 * row — no balances, which have no point-in-time meaning on a historical entry.
 */
sealed interface TransactionDetailUiState {
  data object Loading : TransactionDetailUiState

  /** The row was deleted (or never existed) — the screen shows an unavailable note. */
  data object NotFound : TransactionDetailUiState

  data class Content(
    val kindTitle: UiText,
    /** Drives the amount's colour bucket: Income +pos, Transfer +transfer, Expense neutral. */
    val amountKind: RowKind,
    val amountSign: String,
    val currency: String,
    val amountWhole: String,
    val amountDecimal: String,
    val heroIconKey: String?,
    val heroColorKey: String?,
    /** The hero title — the note; null omits the line entirely. */
    val note: String?,
    val date: UiText,
    /** "Edited · 2 Jan", only when the row was changed after creation. */
    val edited: UiText?,
    val from: PathNode?,
    val to: PathNode?,
    /**
     * Debt rows can't be deleted here — a debt's `origin_transaction_id` FKs back
     * to the row, so a plain delete is rejected. Deletion routes through the debt
     * aggregate once Debts (FLO-18) lands; until then the action is hidden.
     */
    val canDelete: Boolean,
  ) : TransactionDetailUiState
}

/** One end of the money-path rail — an account or a category, with its own glyph/colour. */
data class PathNode(
  val role: UiText,
  val name: UiText,
  val iconKey: String?,
  val colorKey: String?,
)
