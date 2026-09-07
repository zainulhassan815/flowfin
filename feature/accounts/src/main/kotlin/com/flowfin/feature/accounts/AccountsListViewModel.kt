package com.flowfin.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.usecase.ObserveBudgetStatus
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.BudgetPeriod
import com.flowfin.core.model.BudgetStatus
import com.flowfin.core.model.Money
import com.flowfin.core.resources.R
import com.flowfin.core.ui.BudgetProgressUi
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.toCardUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Drives the Accounts tab: the total-holdings hero with its Real / Budget / count
 * breakdown, and the two card sections. Budget cards carry envelope progress,
 * which comes from [ObserveBudgetStatus] rather than being computed here — the
 * daily notification pass reads the same figures.
 */
class AccountsListViewModel(
  accounts: AccountRepository,
  budgetStatus: ObserveBudgetStatus,
  private val money: MoneyFormatter,
) : ViewModel() {

  val uiState: StateFlow<AccountsListUiState> = combine(
    accounts.observeBalances(),
    budgetStatus(),
  ) { balances, budgets ->
    buildState(balances, budgets.associateBy { it.account.id })
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AccountsListUiState.Loading)

  private fun buildState(
    balances: List<AccountBalance>,
    budgetsById: Map<AccountId, BudgetStatus>,
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
        it.toCardUi(accountsById, money, progress = budgetsById[it.account.id]?.toProgressUi())
      },
    )
  }

  private fun BudgetStatus.toProgressUi() = BudgetProgressUi(
    spent = money.displayWhole(spent),
    caption = UiText.Res(
      when (period) {
        BudgetPeriod.MONTH -> R.string.budget_progress_month
        BudgetPeriod.LIFETIME -> R.string.budget_progress_lifetime
      },
      listOf(money.displayWhole(funded)),
    ),
    fraction = fraction,
  )
}

private const val STOP_TIMEOUT_MS = 5_000L
