package com.flowfin.feature.accounts

import app.cash.turbine.test
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountFlow
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    accounts = FakeAccountRepository(balances = balances),
    transactions = FakeTransactionRepository(byAccount = feed, flow = flow),
    categories = FakeCategoryRepository(categories),
    clock = FixedClock(now),
    money = MoneyFormatter(),
  )

  @Test
  fun `an account not among the active balances yields NotFound`() = runTest {
    val missing = AccountId(Uuid.random())
    viewModel(missing, balances = emptyList()).uiState.test {
      assertEquals(AccountDetailUiState.NotFound, resolved(AccountDetailUiState.Loading))
    }
  }

  @Test
  fun `maps the account hero, balance, flow, and grouped activity`() = runTest {
    val bank = account("Bank", AccountType.REAL)
    val salary = category("Salary", CategoryScope.INCOME, icon = "payments", color = "salary")
    // Amounts stay under a lakh so the assertion doesn't depend on the host JVM's
    // grouping locale (Indian lakh grouping is exercised on-device, not here).
    val feed = listOf(
      income(to = bank.id, amount = Money(8_000_000), category = salary.id, note = "Monthly salary"), // Rs 80,000
      expense(from = bank.id, amount = Money(1_200_000), category = salary.id), // Rs 12,000
    )

    viewModel(
      id = bank.id,
      balances = listOf(AccountBalance(bank, Money(4_000_000))), // Rs 40,000
      feed = feed,
      flow = AccountFlow(inflow = Money(8_000_000), outflow = Money(4_000_000)),
      categories = listOf(salary),
    ).uiState.test {
      val content = resolved(AccountDetailUiState.Loading) as AccountDetailUiState.Content
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
      // The title already names the category; the row's meta surfaces the note.
      assertEquals("Monthly salary", rows[0].meta)
    }
  }

  @Test
  fun `a transfer is account-relative — positive on the receiver, negative on the payer`() = runTest {
    val bank = account("Bank")
    val cash = account("Cash")
    val transfer = tx(TransactionKind.TRANSFER, from = bank.id, to = cash.id, amount = Money(500_000)) // Rs 5,000
    val balances = listOf(AccountBalance(bank, Money.ZERO), AccountBalance(cash, Money(500_000)))

    // Viewed on Cash (the destination): money arrived.
    viewModel(cash.id, balances, feed = listOf(transfer), flow = AccountFlow(Money(500_000), Money.ZERO)).uiState.test {
      val row = (resolved(AccountDetailUiState.Loading) as AccountDetailUiState.Content).activity.single().rows.single()
      assertEquals("+5,000", row.amount)
      assertEquals(RowKind.Transfer, row.kind)
      assertEquals("From Bank", row.meta)
    }

    // Viewed on Bank (the source): money left.
    viewModel(bank.id, balances, feed = listOf(transfer), flow = AccountFlow(Money.ZERO, Money(500_000))).uiState.test {
      val row = (resolved(AccountDetailUiState.Loading) as AccountDetailUiState.Content).activity.single().rows.single()
      assertEquals("−5,000", row.amount)
      assertEquals("To Cash", row.meta)
    }
  }

  @Test
  fun `an empty ledger hides the flow strip and flags no activity`() = runTest {
    val cash = account("Cash", AccountType.REAL)
    viewModel(
      id = cash.id,
      balances = listOf(AccountBalance(cash, Money(2_500_000))),
      feed = emptyList(),
      flow = AccountFlow(Money.ZERO, Money.ZERO),
    ).uiState.test {
      val content = resolved(AccountDetailUiState.Loading) as AccountDetailUiState.Content
      assertTrue(content.noActivity)
      assertNull(content.flow)
      assertTrue(content.activity.isEmpty())
    }
  }

  @Test
  fun `a budget account carries its parent name and budget type`() = runTest {
    val bank = account("Bank", AccountType.REAL)
    val food = account("Food", AccountType.BUDGET, parent = bank.id)
    viewModel(
      id = food.id,
      balances = listOf(AccountBalance(bank, Money(4_000_000)), AccountBalance(food, Money(950_000))),
    ).uiState.test {
      val content = resolved(AccountDetailUiState.Loading) as AccountDetailUiState.Content
      assertTrue(content.isBudget)
      assertEquals("Bank", content.meta)
    }
  }
}

// Transaction fixtures — local to this test; recordedAt is fixed (EPOCH) so the
// rows group under a single day. The flow strip is supplied directly, not derived.
private fun income(to: AccountId, amount: Money, category: CategoryId, note: String? = null): Transaction =
  tx(TransactionKind.INCOME, to = to, amount = amount, category = category, note = note)

private fun expense(from: AccountId, amount: Money, category: CategoryId, note: String? = null): Transaction =
  tx(TransactionKind.EXPENSE, from = from, amount = amount, category = category, note = note)

private fun tx(
  kind: TransactionKind,
  from: AccountId? = null,
  to: AccountId? = null,
  amount: Money,
  category: CategoryId? = null,
  note: String? = null,
): Transaction = Transaction(
  id = TransactionId(Uuid.random()),
  kind = kind,
  fromAccountId = from,
  toAccountId = to,
  amount = amount,
  categoryId = category,
  note = note,
  recordedAt = EPOCH,
  recurringId = null,
  debtId = null,
  createdAt = EPOCH,
  updatedAt = EPOCH,
)
