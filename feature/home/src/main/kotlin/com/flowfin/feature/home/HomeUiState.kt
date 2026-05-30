package com.flowfin.feature.home

import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText

/**
 * Home's states, derived in the ViewModel. [Empty] is the F full-empty treatment
 * (no accounts yet); within [Content], [recent] carries its own empty variants and
 * an empty [pending] simply drops the Pending section.
 * There is no error state — a read failure is catastrophic, not a Home state.
 */
sealed interface HomeUiState {
  data object Loading : HomeUiState

  /** [currency] is the hero's symbol, sourced from the money formatter. */
  data class Empty(val currency: String) : HomeUiState

  data class Content(
    val currency: String,
    val totalWhole: String,
    val totalDecimal: String,
    val allocated: String,
    val aside: HeroAside?,
    val realTotal: String,
    val realAccounts: List<AccountCardUi>,
    val budgetTotal: String,
    val budgetAccounts: List<AccountCardUi>,
    val pending: List<PendingRowUi>,
    val pendingTotal: Int,
    val recent: RecentSection,
  ) : HomeUiState
}

/** The Recent section's content. Home's feed is this calendar week's activity;
 *  the two empty variants turn on whether anything was ever logged. */
sealed interface RecentSection {
  /** This week's transactions, grouped by day. */
  data class Activity(val groups: List<RecentGroup>) : RecentSection

  /** No transactions exist yet — shows the "first entry" prompt. */
  data object NoEntries : RecentSection

  /** History exists, but nothing this week. [lastEntry] e.g. "6 days ago". */
  data class Quiet(val lastEntry: UiText) : RecentSection
}

/** The hero's right-hand aside beside "Allocated". */
sealed interface HeroAside {
  /** Established history: the month-over-month delta. [percent] omits the "this
   *  month" suffix so the hero can color the two parts differently. */
  data class Trend(val percent: String, val rising: Boolean) : HeroAside

  /** Too little history for an honest trend yet — e.g. "Day 5", building a picture. */
  data class Settling(val dayLabel: UiText) : HeroAside
}

/** [Due] maps to the warning tint, [Late] to the negative tint. */
enum class PendingUrgency { Due, Late }

/** [amountAccount] is the pre-joined "Rs 5,000 · Bank" line; [statusText] the
 *  colored tail ("Due today" / "3 days late"). */
data class PendingRowUi(
  val id: RecurringScheduleId,
  val name: String,
  val amountAccount: String,
  val statusText: UiText,
  val urgency: PendingUrgency,
)

data class RecentGroup(
  val dateLabel: UiText,
  val rows: List<TxRowUi>,
)
