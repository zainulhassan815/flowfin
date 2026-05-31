package com.flowfin.feature.accounts

import app.cash.turbine.test
import arrow.core.Either
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionId
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class AccountsListViewModelTest {

  @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private fun viewModel(balances: List<AccountBalance>, spend: Map<AccountId, Money> = emptyMap()) =
    AccountsListViewModel(BalancesRepo(balances), SpendRepo(spend), MoneyFormatter())

  @Test
  fun `no accounts yields the empty state`() = runTest {
    viewModel(balances = emptyList()).uiState.test {
      assertEquals(AccountsListUiState.Empty, resolved())
    }
  }

  @Test
  fun `splits real and budget, totals them, and computes envelope progress`() = runTest {
    val bank = acct("Bank", AccountType.REAL)
    val food = acct("Food", AccountType.BUDGET, parent = bank.id)
    val balances = listOf(
      AccountBalance(bank, Money(4_000_000)),  // Rs 40,000
      AccountBalance(food, Money(950_000)),    // Rs 9,500 remaining
    )

    viewModel(balances, spend = mapOf(food.id to Money(650_000))).uiState.test {
      val content = resolved() as AccountsListUiState.Content
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

/** stateIn starts at Loading; step past it to the computed state. */
private suspend fun app.cash.turbine.ReceiveTurbine<AccountsListUiState>.resolved(): AccountsListUiState {
  val first = awaitItem()
  return if (first is AccountsListUiState.Loading) awaitItem() else first
}

private val T0 = Instant.fromEpochSeconds(0)

private fun acct(name: String, type: AccountType, parent: AccountId? = null): Account = Account(
  id = AccountId(Uuid.random()),
  name = name,
  type = type,
  currency = "PKR",
  parentAccountId = parent,
  openingBalance = Money.ZERO,
  color = null,
  icon = null,
  displayOrder = 0,
  createdAt = T0,
  updatedAt = T0,
  archivedAt = null,
)

private class BalancesRepo(private val balances: List<AccountBalance>) : AccountRepository {
  override fun observeBalances(): Flow<List<AccountBalance>> = flowOf(balances)
  override fun observeActiveAccounts(): Flow<List<Account>> = throw NotImplementedError()
  override fun observeAccountsByType(type: AccountType): Flow<List<Account>> = throw NotImplementedError()
  override fun observeBudgets(parent: AccountId): Flow<List<Account>> = throw NotImplementedError()
  override fun observeTotalBalance(): Flow<Money> = throw NotImplementedError()
  override suspend fun getById(id: AccountId): Account? = throw NotImplementedError()
  override suspend fun balanceOf(id: AccountId): Money? = throw NotImplementedError()
  override suspend fun activeNameExists(name: String): Boolean = throw NotImplementedError()
  override suspend fun create(name: String, type: AccountType, currency: String, parentAccountId: AccountId?, openingBalance: Money, color: String?, icon: String?, displayOrder: Int): Either<AccountError, Account> = throw NotImplementedError()
  override suspend fun updateBasics(id: AccountId, name: String, color: String?, icon: String?, displayOrder: Int): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun archive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun unarchive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
}

private class SpendRepo(private val spend: Map<AccountId, Money>) : TransactionRepository {
  override fun observeExpenseByAccount(): Flow<Map<AccountId, Money>> = flowOf(spend)
  override fun recentFeed(limit: Long): Flow<List<Transaction>> = throw NotImplementedError()
  override fun observeNetChange(startAt: Instant, endAt: Instant): Flow<Money> = throw NotImplementedError()
  override fun observeByAccount(accountId: AccountId, limit: Long, offset: Long): Flow<List<Transaction>> = throw NotImplementedError()
  override suspend fun getById(id: TransactionId): Transaction? = throw NotImplementedError()
  override suspend fun record(draft: TransactionDraft): Either<TransactionError, Transaction> = throw NotImplementedError()
  override suspend fun updateContent(id: TransactionId, amount: Money, categoryId: CategoryId?, note: String?, recordedAt: Instant): Either<TransactionError, Unit> = throw NotImplementedError()
  override suspend fun delete(id: TransactionId): Either<TransactionError, Unit> = throw NotImplementedError()
}
