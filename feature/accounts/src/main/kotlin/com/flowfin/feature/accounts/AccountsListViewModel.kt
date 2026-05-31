package com.flowfin.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.designsystem.component.BudgetProgress
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Money
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.MoneyFormatter
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
      real = real.map { it.toCardUi(accountsById, spendByAccount) },
      budgets = budget.map { it.toCardUi(accountsById, spendByAccount) },
    )
  }

  private fun AccountBalance.toCardUi(
    accountsById: Map<AccountId, Account>,
    spendByAccount: Map<AccountId, Money>,
  ): AccountCardUi = AccountCardUi(
    id = account.id,
    meta = if (account.isBudget) account.parentAccountId?.let { accountsById[it]?.name }.orEmpty() else "",
    name = account.name,
    balanceWhole = money.whole(balance),
    balanceDecimal = money.fraction(balance),
    iconKey = account.icon ?: if (account.isBudget) "wallet" else "bank",
    colorKey = account.color ?: if (account.isBudget) "other" else "bank",
    isBudget = account.isBudget,
    progress = if (account.isBudget) budgetProgress(balance, spendByAccount[account.id] ?: Money.ZERO) else null,
  )

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
