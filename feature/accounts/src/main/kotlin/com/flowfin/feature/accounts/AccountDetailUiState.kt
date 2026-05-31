package com.flowfin.feature.accounts

import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText

/**
 * Account detail's states. [NotFound] covers an account that's no longer active
 * (archived or removed while the screen was open) — the only honest non-content
 * outcome; a read failure is a crash, not a screen state.
 *
 * Within [Content], emptiness lives at two scopes: [flow] is null when nothing
 * moved this month (the strip drops), and [noActivity] marks an account whose
 * ledger is empty (the balance reads as an opening figure, activity shows its
 * first-entry hint).
 */
sealed interface AccountDetailUiState {
  data object Loading : AccountDetailUiState

  data object NotFound : AccountDetailUiState

  data class Content(
    val name: String,
    /** Header eyebrow / title: "Real account" or "Budget". */
    val typeLabel: UiText,
    /** Hero pill: "Real" or "Budget". */
    val typeTag: UiText,
    /** Budget's parent-account name; empty for real accounts. */
    val meta: String,
    val iconKey: String?,
    val colorKey: String?,
    val isBudget: Boolean,
    val currency: String,
    val balanceWhole: String,
    val balanceDecimal: String,
    /** True when the ledger is empty — drives the Opening tag and the empty hint. */
    val noActivity: Boolean,
    /** This month's In / Out / Net; null when nothing moved this month. */
    val flow: FlowUi?,
    val activity: List<TxGroup>,
  ) : AccountDetailUiState
}

/** This month's movement strip. Amounts are pre-formatted (no currency symbol —
 *  the cells render a small "Rs" themselves); [net] carries its own +/− sign. */
data class FlowUi(
  /** The month being summarised, short form — "Dec". */
  val month: String,
  val inflow: String,
  val outflow: String,
  val net: String,
  val netSign: FlowSign,
)

/** Colours the net figure: [Up] positive, [Down] negative, [Flat] neutral. */
enum class FlowSign { Up, Down, Flat }

/** A day's worth of the account's activity, newest day first. */
data class TxGroup(
  val dateLabel: UiText,
  val rows: List<TxRowUi>,
)
