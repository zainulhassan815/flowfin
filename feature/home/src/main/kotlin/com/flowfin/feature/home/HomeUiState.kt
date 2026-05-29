package com.flowfin.feature.home

/**
 * UI state for the Home screen. All money is pre-formatted into display strings
 * by the ViewModel, so the screen stays free of money/locale formatting. Grows as
 * more sections are wired in; [recent] is an interim plain-text list until the
 * Home design pass replaces it with hydrated transaction rows.
 */
data class HomeUiState(
  val currency: String,
  val balanceWhole: String,
  val balanceDecimal: String,
  val allocated: String,
  val trend: String,
  val recent: List<String> = emptyList(),
) {
  companion object {
    val Empty = HomeUiState(
      currency = "Rs",
      balanceWhole = "0",
      balanceDecimal = ".00",
      allocated = "Rs 0.00",
      trend = "—",
    )
  }
}
