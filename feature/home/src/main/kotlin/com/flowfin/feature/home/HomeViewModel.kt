package com.flowfin.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Drives the Home screen from the domain: the headline total, the allocated
 * (sum of budget balances) and a recent-activity list, recomputed whenever any
 * of the underlying tables change.
 */
class HomeViewModel(
  accounts: AccountRepository,
  transactions: TransactionRepository,
) : ViewModel() {

  val uiState: StateFlow<HomeUiState> = combine(
    accounts.observeTotalBalance(),
    accounts.observeBalances(),
    transactions.recentFeed(limit = RECENT_LIMIT),
  ) { total, balances, recent ->
    val allocated = balances
      .filter { it.account.isBudget }
      .fold(Money.ZERO) { sum, entry -> sum + entry.balance }

    HomeUiState(
      currency = CURRENCY,
      balanceWhole = whole(total),
      balanceDecimal = fraction(total),
      allocated = "$CURRENCY ${plain(allocated)}",
      trend = "—",
      recent = recent.map(::recentLine),
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Empty)
}

private const val CURRENCY = "Rs"
private const val RECENT_LIMIT = 20L
private const val STOP_TIMEOUT_MS = 5_000L

private val grouping: NumberFormat = NumberFormat.getIntegerInstance(Locale.US)

private fun whole(money: Money): String {
  val sign = if (money.minorUnits < 0) "-" else ""
  return sign + grouping.format(money.minorUnits.absoluteValue / 100)
}

private fun fraction(money: Money): String =
  "." + (money.minorUnits.absoluteValue % 100).toString().padStart(2, '0')

private fun plain(money: Money): String = whole(money) + fraction(money)

private fun recentLine(transaction: Transaction): String {
  val label = transaction.kind.name.lowercase().replaceFirstChar(Char::uppercase)
  return "$label · $CURRENCY ${plain(transaction.amount)}"
}
