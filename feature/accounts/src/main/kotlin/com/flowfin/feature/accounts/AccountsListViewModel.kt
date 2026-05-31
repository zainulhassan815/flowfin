package com.flowfin.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.designsystem.component.BudgetProgress
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Money
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.toCardUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Drives the Accounts tab: the total-holdings hero with its Real / Budget / count
 * breakdown, and the two card sections. Budget cards carry envelope progress —
 * `funded = remaining balance + spend` — joined from the per-account expense stream.
 */
class AccountsListViewModel(
  accounts: AccountRepository,
  transactions: TransactionRepository,
  private val money: MoneyFormatter,
) : ViewModel() {

  val uiState: StateFlow<AccountsListUiState> = combine(
    accounts.observeBalances(),
    transactions.observeExpenseByAccount(),
  ) { balances, spendByAccount ->
    buildState(balances, spendByAccount)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AccountsListUiState.Loading)

  private fun buildState(
    balances: List<AccountBalance>,
    spendByAccount: Map<AccountId, Money>,
  ): AccountsListUiState {
    if (balances.isEmpty()) return AccountsListUiState.Empty

    val accountsById = balances.associate { it.account.id to it.account }
    val real = balances.filter { it.account.isReal }
    val budget = balances.filter { it.account.isBudget }
    val realSum = real.fold(Money.ZERO) { sum, b -> sum + b.balance }
    val budgetSum = budget.fold(Money.ZERO) { sum, b -> sum + b.balance }
    val total = realSum + budgetSum

    return AccountsListUiState.Content(
      currency = money.symbol,
      totalWhole = money.whole(total),
      totalDecimal = money.fraction(total),
      realTotal = money.displayWhole(realSum),
      budgetTotal = money.displayWhole(budgetSum),
      accountCount = balances.size,
      real = real.map { it.toCardUi(accountsById, money) },
      budgets = budget.map {
        it.toCardUi(accountsById, money, progress = budgetProgress(it.balance, spendByAccount[it.account.id] ?: Money.ZERO))
      },
    )
  }

  /** Envelope progress: spent of funded, where funded is what's left plus what's gone. */
  private fun budgetProgress(remaining: Money, spent: Money): BudgetProgress {
    val funded = remaining + spent
    val fraction = if (funded.isPositive) {
      (spent.minorUnits.toFloat() / funded.minorUnits).coerceIn(0f, 1f)
    } else {
      0f
    }
    return BudgetProgress(spent = money.displayWhole(spent), total = money.displayWhole(funded), fraction = fraction)
  }
}

private const val STOP_TIMEOUT_MS = 5_000L
