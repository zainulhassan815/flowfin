package com.flowfin.feature.accounts

import app.cash.turbine.test
import arrow.core.Either
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.error.CategoryError
import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountFlow
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class AccountDetailViewModelTest {

  @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private val now = Instant.parse("2025-12-27T10:00:00Z")

  private fun viewModel(
    id: AccountId,
    balances: List<AccountBalance>,
    feed: List<Transaction> = emptyList(),
    flow: AccountFlow = AccountFlow(Money.ZERO, Money.ZERO),
    categories: List<Category> = emptyList(),
  ) = AccountDetailViewModel(
    accountId = id,
    accounts = DetailAccountsRepo(balances),
    transactions = DetailTxRepo(feed, flow),
    categories = DetailCategoriesRepo(categories),
    clock = DetailClock(now),
    money = MoneyFormatter(),
  )

  @Test
  fun `an account not among the active balances yields NotFound`() = runTest {
    val missing = AccountId(Uuid.random())
    viewModel(missing, balances = emptyList()).uiState.test {
      assertEquals(AccountDetailUiState.NotFound, resolved())
    }
  }

  @Test
  fun `maps the account hero, balance, flow, and grouped activity`() = runTest {
    val bank = detailAccount("Bank", AccountType.REAL)
    val salary = category("Salary", CategoryScope.INCOME, icon = "payments", color = "salary")
    // Amounts stay under a lakh so the assertion doesn't depend on the host JVM's
    // grouping locale (Indian lakh grouping is exercised on-device, not here).
    val feed = listOf(
      income(to = bank.id, amount = Money(8_000_000), category = salary.id), // Rs 80,000
      expense(from = bank.id, amount = Money(1_200_000), category = salary.id), // Rs 12,000
    )

    viewModel(
      id = bank.id,
      balances = listOf(AccountBalance(bank, Money(4_000_000))), // Rs 40,000
      feed = feed,
      flow = AccountFlow(inflow = Money(8_000_000), outflow = Money(4_000_000)),
      categories = listOf(salary),
    ).uiState.test {
      val content = resolved() as AccountDetailUiState.Content
      assertEquals("Bank", content.name)
      assertEquals(false, content.isBudget)
      assertEquals("40,000", content.balanceWhole)
      assertEquals(false, content.noActivity)

      val flow = content.flow!!
      assertEquals("Dec", flow.month)
      assertEquals("80,000", flow.inflow)
      assertEquals("40,000", flow.outflow)
      assertEquals("+40,000", flow.net) // net = in − out = 40,000
      assertEquals(FlowSign.Up, flow.netSign)

      // Both rows fall on the same day, so one group with two rows.
      assertEquals(1, content.activity.size)
      val rows = content.activity.single().rows
      assertEquals(2, rows.size)
      assertEquals("+80,000", rows[0].amount)
      assertEquals(RowKind.Income, rows[0].kind)
      assertEquals(UiText.Raw("Salary"), rows[0].name)
    }
  }

  @Test
  fun `an empty ledger hides the flow strip and flags no activity`() = runTest {
    val cash = detailAccount("Cash", AccountType.REAL)
    viewModel(
      id = cash.id,
      balances = listOf(AccountBalance(cash, Money(2_500_000))),
      feed = emptyList(),
      flow = AccountFlow(Money.ZERO, Money.ZERO),
    ).uiState.test {
      val content = resolved() as AccountDetailUiState.Content
      assertTrue(content.noActivity)
      assertNull(content.flow)
      assertTrue(content.activity.isEmpty())
    }
  }

  @Test
  fun `a budget account carries its parent name and budget type`() = runTest {
    val bank = detailAccount("Bank", AccountType.REAL)
    val food = detailAccount("Food", AccountType.BUDGET, parent = bank.id)
    viewModel(
      id = food.id,
      balances = listOf(AccountBalance(bank, Money(4_000_000)), AccountBalance(food, Money(950_000))),
    ).uiState.test {
      val content = resolved() as AccountDetailUiState.Content
      assertTrue(content.isBudget)
      assertEquals("Bank", content.meta)
    }
  }
}

/** stateIn starts at Loading; step past it to the computed state. */
private suspend fun app.cash.turbine.ReceiveTurbine<AccountDetailUiState>.resolved(): AccountDetailUiState {
  val first = awaitItem()
  return if (first is AccountDetailUiState.Loading) awaitItem() else first
}

private val DETAIL_T0 = Instant.parse("2025-12-27T09:00:00Z")

private fun detailAccount(name: String, type: AccountType, parent: AccountId? = null): Account = Account(
  id = AccountId(Uuid.random()),
  name = name,
  type = type,
  currency = "PKR",
  parentAccountId = parent,
  openingBalance = Money.ZERO,
  color = null,
  icon = null,
  displayOrder = 0,
  createdAt = DETAIL_T0,
  updatedAt = DETAIL_T0,
  archivedAt = null,
)

private fun category(name: String, scope: CategoryScope, icon: String?, color: String?): Category = Category(
  id = CategoryId(Uuid.random()),
  name = name,
  scope = scope,
  isDefault = false,
  icon = icon,
  color = color,
  displayOrder = 0,
  createdAt = DETAIL_T0,
  updatedAt = DETAIL_T0,
  archivedAt = null,
)

private fun income(to: AccountId, amount: Money, category: CategoryId): Transaction = detailTx(
  kind = TransactionKind.INCOME, to = to, amount = amount, category = category,
)

private fun expense(from: AccountId, amount: Money, category: CategoryId): Transaction = detailTx(
  kind = TransactionKind.EXPENSE, from = from, amount = amount, category = category,
)

private fun detailTx(
  kind: TransactionKind,
  from: AccountId? = null,
  to: AccountId? = null,
  amount: Money,
  category: CategoryId? = null,
): Transaction = Transaction(
  id = TransactionId(Uuid.random()),
  kind = kind,
  fromAccountId = from,
  toAccountId = to,
  amount = amount,
  categoryId = category,
  note = null,
  recordedAt = DETAIL_T0,
  recurringId = null,
  debtId = null,
  createdAt = DETAIL_T0,
  updatedAt = DETAIL_T0,
)

private class DetailClock(private val instant: Instant) : Clock {
  override fun now(): Instant = instant
}

private class DetailAccountsRepo(private val balances: List<AccountBalance>) : AccountRepository {
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

private class DetailTxRepo(private val feed: List<Transaction>, private val flow: AccountFlow) : TransactionRepository {
  override fun observeByAccount(accountId: AccountId, limit: Long, offset: Long): Flow<List<Transaction>> = flowOf(feed)
  override fun observeFlow(accountId: AccountId, startAt: Instant, endAt: Instant): Flow<AccountFlow> = flowOf(flow)
  override fun recentFeed(limit: Long): Flow<List<Transaction>> = throw NotImplementedError()
  override fun observeNetChange(startAt: Instant, endAt: Instant): Flow<Money> = throw NotImplementedError()
  override fun observeExpenseByAccount(): Flow<Map<AccountId, Money>> = throw NotImplementedError()
  override suspend fun getById(id: TransactionId): Transaction? = throw NotImplementedError()
  override suspend fun record(draft: TransactionDraft): Either<TransactionError, Transaction> = throw NotImplementedError()
  override suspend fun updateContent(id: TransactionId, amount: Money, categoryId: CategoryId?, note: String?, recordedAt: Instant): Either<TransactionError, Unit> = throw NotImplementedError()
  override suspend fun delete(id: TransactionId): Either<TransactionError, Unit> = throw NotImplementedError()
}

private class DetailCategoriesRepo(private val categories: List<Category>) : CategoryRepository {
  override fun observeAll(): Flow<List<Category>> = flowOf(categories)
  override fun observeByScope(scope: CategoryScope): Flow<List<Category>> = throw NotImplementedError()
  override suspend fun ensureDefaultsSeeded(): Either<CategoryError, Unit> = throw NotImplementedError()
  override suspend fun getById(id: CategoryId): Category? = throw NotImplementedError()
  override suspend fun createCustom(name: String, scope: CategoryScope, icon: String?, color: String?, displayOrder: Int): Either<CategoryError, Category> = throw NotImplementedError()
  override suspend fun updateCustom(id: CategoryId, name: String, icon: String?, color: String?, displayOrder: Int): Either<CategoryError, Unit> = throw NotImplementedError()
  override suspend fun archive(id: CategoryId): Either<CategoryError, Unit> = throw NotImplementedError()
  override suspend fun unarchive(id: CategoryId): Either<CategoryError, Unit> = throw NotImplementedError()
}
