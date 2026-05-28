package com.flowfin.feature.home

/**
 * UI state for the Home screen. All money is pre-formatted into display strings
 * by the (future) ViewModel, so the screen itself stays free of money/locale
 * formatting. This grows as more sections are wired in — currently the hero.
 */
data class HomeUiState(
  val currency: String,
  val balanceWhole: String,
  val balanceDecimal: String,
  val allocated: String,
  val trend: String,
)
