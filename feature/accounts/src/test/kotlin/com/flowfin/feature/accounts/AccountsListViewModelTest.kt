package com.flowfin.feature.accounts

import app.cash.turbine.test
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Money
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountsListViewModelTest {

  @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private fun viewModel(balances: List<AccountBalance>, spend: Map<AccountId, Money> = emptyMap()) =
    AccountsListViewModel(
      FakeAccountRepository(balances = balances),
      FakeTransactionRepository(expenseByAccount = spend),
      MoneyFormatter(),
    )

  @Test
  fun `no accounts yields the empty state`() = runTest {
    viewModel(balances = emptyList()).uiState.test {
      assertEquals(AccountsListUiState.Empty, resolved(AccountsListUiState.Loading))
    }
  }

  @Test
  fun `splits real and budget, totals them, and computes envelope progress`() = runTest {
    val bank = account("Bank", AccountType.REAL)
    val food = account("Food", AccountType.BUDGET, parent = bank.id)
    val balances = listOf(
      AccountBalance(bank, Money(4_000_000)),  // Rs 40,000
      AccountBalance(food, Money(950_000)),    // Rs 9,500 remaining
    )

    viewModel(balances, spend = mapOf(food.id to Money(650_000))).uiState.test {
      val content = resolved(AccountsListUiState.Loading) as AccountsListUiState.Content
      assertEquals(2, content.accountCount)
      assertEquals("Rs 40,000", content.realTotal)
      assertEquals("Rs 9,500", content.budgetTotal)
      assertEquals(1, content.real.size)
      assertEquals(1, content.budgets.size)

      val progress = content.budgets.single().progress!!
      assertEquals("Rs 6,500", progress.spent)        // spend
      assertEquals("Rs 16,000", progress.total)       // funded = remaining + spend
      assertEquals(0.40625f, progress.fraction)        // 6,500 / 16,000
    }
  }
}
