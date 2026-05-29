package com.flowfin.feature.home

import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.TxRowUi

/**
 * Home's states, derived in the ViewModel. [Empty] is the F full-empty treatment
 * (no accounts yet); within [Content], an empty [recent] is the S section hint and
 * an empty [pending] simply drops the Pending section.
 * There is no error state — a read failure is catastrophic, not a Home state.
 */
sealed interface HomeUiState {
  data object Loading : HomeUiState

  data object Empty : HomeUiState

  data class Content(
    val totalWhole: String,
    val totalDecimal: String,
    val allocated: String,
    val trend: HomeTrend?,
    val realTotal: String,
    val realAccounts: List<AccountCardUi>,
    val budgetTotal: String,
    val budgetAccounts: List<AccountCardUi>,
    val pending: List<PendingRowUi>,
    val recent: List<RecentGroup>,
  ) : HomeUiState
}

/** [percent] omits the "this month" suffix so the hero can render the two parts
 *  in different colors. */
data class HomeTrend(val percent: String, val rising: Boolean)

/** [Due] maps to the warning tint, [Late] to the negative tint. */
enum class PendingUrgency { Due, Late }

/** [amountAccount] is the pre-joined "Rs 5,000 · Bank" line; [statusText] the
 *  colored tail ("Due today" / "3 days late"). */
data class PendingRowUi(
  val id: RecurringScheduleId,
  val name: String,
  val amountAccount: String,
  val statusText: String,
  val urgency: PendingUrgency,
)

data class RecentGroup(
  val dateLabel: String,
  val rows: List<TxRowUi>,
)
