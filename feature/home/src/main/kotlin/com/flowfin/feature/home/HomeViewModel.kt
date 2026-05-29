package com.flowfin.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.TxRowUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Drives Home from the domain: the headline total, allocated (sum of budget
 * balances), the account cards, and a hydrated recent feed — the feed is joined
 * in-memory against accounts (for names) and categories (names/icons/colours).
 */
class HomeViewModel(
  accounts: AccountRepository,
  transactions: TransactionRepository,
  categories: CategoryRepository,
  private val money: MoneyFormatter,
) : ViewModel() {

  val uiState: StateFlow<HomeUiState> = combine(
    accounts.observeBalances(),
    accounts.observeTotalBalance(),
    transactions.recentFeed(RECENT_LIMIT),
    categories.observeAll(),
  ) { balances, total, recent, categoryList ->
    if (balances.isEmpty()) {
      HomeUiState.Empty
    } else {
      val accountsById = balances.associate { it.account.id to it.account }
      val categoriesById = categoryList.associateBy(Category::id)
      val allocated = balances.filter { it.account.isBudget }.fold(Money.ZERO) { sum, b -> sum + b.balance }
      HomeUiState.Content(
        totalWhole = money.whole(total),
        totalDecimal = money.fraction(total),
        allocated = money.display(allocated),
        accounts = balances.map { it.toCardUi() },
        recent = recent.map { it.toRowUi(accountsById, categoriesById) },
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)

  private fun AccountBalance.toCardUi(): AccountCardUi = AccountCardUi(
    id = account.id,
    name = account.name,
    meta = if (account.isBudget) "Budget" else "Account",
    balanceWhole = money.whole(balance),
    balanceDecimal = money.fraction(balance),
    iconKey = account.icon ?: if (account.isBudget) "wallet" else "bank",
    colorKey = account.color ?: if (account.isBudget) "other" else "bank",
    isBudget = account.isBudget,
  )

  private fun Transaction.toRowUi(
    accountsById: Map<AccountId, Account>,
    categoriesById: Map<CategoryId, Category>,
  ): TxRowUi {
    val category = categoryId?.let { categoriesById[it] }
    val fromName = fromAccountId?.let { accountsById[it]?.name }.orEmpty()
    val toName = toAccountId?.let { accountsById[it]?.name }.orEmpty()
    val moneyIn = kind == TransactionKind.INCOME ||
      kind == TransactionKind.DEBT_BORROW ||
      kind == TransactionKind.DEBT_REPAY_IN
    return TxRowUi(
      id = id,
      name = when (kind) {
        TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.name ?: "—"
        TransactionKind.TRANSFER -> "Transfer"
        TransactionKind.ALLOCATION -> "Allocation"
        TransactionKind.REALLOCATION -> "Reallocation"
        TransactionKind.DEBT_BORROW, TransactionKind.DEBT_LEND,
        TransactionKind.DEBT_REPAY_OUT, TransactionKind.DEBT_REPAY_IN -> "Debt"
      },
      meta = when (kind) {
        TransactionKind.INCOME -> toName
        TransactionKind.EXPENSE -> fromName
        TransactionKind.TRANSFER, TransactionKind.ALLOCATION, TransactionKind.REALLOCATION ->
          "$fromName → $toName"
        else -> ""
      },
      amount = (if (moneyIn) "+" else "−") + money.whole(amount),
      decimal = money.fraction(amount),
      kind = when (kind) {
        TransactionKind.INCOME, TransactionKind.DEBT_BORROW, TransactionKind.DEBT_REPAY_IN -> RowKind.Income
        TransactionKind.EXPENSE, TransactionKind.DEBT_LEND, TransactionKind.DEBT_REPAY_OUT -> RowKind.Expense
        TransactionKind.TRANSFER, TransactionKind.ALLOCATION, TransactionKind.REALLOCATION -> RowKind.Transfer
      },
      iconKey = when (kind) {
        TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.icon
        else -> "sync_alt"
      },
      colorKey = when (kind) {
        TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.color
        else -> null
      },
    )
  }
}

private const val RECENT_LIMIT = 20L
private const val STOP_TIMEOUT_MS = 5_000L
