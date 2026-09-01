package com.flowfin.feature.transactions

import app.cash.turbine.test
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.ui.MoneyFormatter
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

class TransactionsListViewModelTest {

  @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private val now = Instant.parse("2026-12-27T10:00:00Z")
  private val bank = account("Bank", AccountType.REAL)
  private val cash = account("Cash", AccountType.REAL)
  private val salary = category("Salary", CategoryScope.INCOME, color = "salary")
  private val food = category("Food", CategoryScope.EXPENSE, color = "food")

  // Amounts stay under a lakh so assertions don't depend on the host JVM's grouping locale.
  private val incomeDec = tx(TransactionKind.INCOME, Money(80_000), Instant.parse("2026-12-27T09:00:00Z"), to = bank.id, category = salary.id)
  private val expenseDec = tx(TransactionKind.EXPENSE, Money(30_000), Instant.parse("2026-12-26T09:00:00Z"), from = bank.id, category = food.id)
  private val transferNov = tx(TransactionKind.TRANSFER, Money(20_000), Instant.parse("2026-11-10T09:00:00Z"), from = bank.id, to = cash.id)

  private fun viewModel(feed: List<Transaction>) = TransactionsListViewModel(
    transactions = FakeTransactionRepository(feed),
    accounts = FakeAccountRepository(listOf(bank, cash)),
    categories = FakeCategoryRepository(listOf(salary, food)),
    debts = FakeDebtRepository(),
    clock = FixedClock(now),
    money = MoneyFormatter(),
  )

  @Test
  fun `an empty feed yields Empty`() = runTest {
    viewModel(emptyList()).uiState.test {
      assertEquals(TransactionsListUiState.Empty, resolved(TransactionsListUiState.Loading))
    }
  }

  @Test
  fun `groups the ledger newest-first by month and day with an external net`() = runTest {
    // Feed is newest-first, as the repository returns it.
    viewModel(listOf(incomeDec, expenseDec, transferNov)).uiState.test {
      val state = resolved(TransactionsListUiState.Loading)
      assertTrue(state is TransactionsListUiState.Content)

      val months = state.months
      assertEquals(listOf("2026-12", "2026-11"), months.map { it.id })

      // December: income on the 27th, expense on the 26th — two day groups, newest first.
      val december = months[0]
      assertEquals(2, december.days.size)
      assertTrue(december.days.first().isToday) // the 27th is "today"
      // Net = 800 in − 300 out = +500 (transfers are internal, excluded).
      assertEquals("+500", december.net)
      assertTrue(december.netPositive)

      // November holds only a transfer — an internal move — so it shows no net.
      assertNull(months[1].net)
    }
  }

  @Test
  fun `a filter narrows to its kind and drops the net`() = runTest {
    val vm = viewModel(listOf(incomeDec, expenseDec, transferNov))
    vm.uiState.test {
      resolved(TransactionsListUiState.Loading) // All

      vm.setFilter(TxFilter.Out)
      val out = awaitItem()
      assertTrue(out is TransactionsListUiState.Content)
      // Only the December expense survives — one month, one day, one row.
      assertEquals(listOf("2026-12"), out.months.map { it.id })
      assertEquals(1, out.months[0].days.single().rows.size)
      // A filtered slice has no meaningful net.
      assertNull(out.months[0].net)
    }
  }

  @Test
  fun `a filter that matches nothing yields content with no months`() = runTest {
    val vm = viewModel(listOf(expenseDec)) // expenses only
    vm.uiState.test {
      resolved(TransactionsListUiState.Loading)

      vm.setFilter(TxFilter.In)
      val state = awaitItem()
      assertTrue(state is TransactionsListUiState.Content)
      assertEquals(TxFilter.In, state.filter)
      assertTrue(state.months.isEmpty())
    }
  }
}
