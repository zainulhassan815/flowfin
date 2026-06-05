package com.flowfin.feature.transactions

import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText

/**
 * The full ledger, newest-first, reached from Home's "Recent · All / History".
 *
 * Emptiness lives at two scopes: a top-level [Empty] when nothing has ever been
 * logged, and an empty [Content.months] when the active [TxFilter] hides every
 * row (the screen shows a filtered-empty hint, the chips stay live). A read
 * failure is a crash, not a screen state — there is no error arm.
 */
sealed interface TransactionsListUiState {
  data object Loading : TransactionsListUiState

  data object Empty : TransactionsListUiState

  data class Content(
    val filter: TxFilter,
    /** Months newest-first; empty means the filter matched nothing. */
    val months: List<MonthSection>,
  ) : TransactionsListUiState
}

/**
 * Which slice of the ledger is shown, as a display [bucket]; null ([All]) is the
 * whole ledger. The ViewModel turns the bucket into the domain kinds to query via
 * the shared `TransactionKind.rowKind()` mapping, so the filter, the row colours,
 * and the net never drift from one another.
 */
enum class TxFilter(val bucket: RowKind?) {
  All(null),
  In(RowKind.Income),
  Out(RowKind.Expense),
  Transfers(RowKind.Transfer),
}

/**
 * A month's worth of the ledger. [net] is this month's external In − Out
 * (transfers are internal and excluded), pre-formatted and signed; it is null
 * while a filter is active, since a partial slice has no meaningful net.
 */
data class MonthSection(
  /** Stable key, e.g. "2026-12". */
  val id: String,
  /** Heading, e.g. "December 2026". */
  val label: UiText,
  val currency: String,
  /** Signed whole figure ("+37,500" / "−8,200"); null when filtered. */
  val net: String?,
  val netPositive: Boolean,
  val days: List<DayGroup>,
)

/** A single day inside a month, newest day first. */
data class DayGroup(
  val dateLabel: UiText,
  val isToday: Boolean,
  val rows: List<TxRowUi>,
)
