package com.flowfin.feature.accounts

import app.cash.turbine.test
import com.flowfin.core.domain.usecase.ObserveBudgetStatus
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Money
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.model.RecurringStatus
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class AccountsListViewModelTest {

  @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private fun viewModel(
    balances: List<AccountBalance>,
    spend: Map<AccountId, Money> = emptyMap(),
    spendThisMonth: Map<AccountId, Money>? = null,
    schedules: List<RecurringSchedule> = emptyList(),
  ): AccountsListViewModel {
    val accounts = FakeAccountRepository(balances = balances)
    val transactions = FakeTransactionRepository(
      expenseByAccount = spend,
      expenseThisMonth = spendThisMonth,
    )
    return AccountsListViewModel(
      accounts,
      ObserveBudgetStatus(
        accounts,
        transactions,
        FakeRecurringRepository(active = schedules),
        Clock.System,
        TimeZone.UTC,
      ),
      MoneyFormatter(),
    )
  }

  @Test
  fun `no accounts yields the empty state`() = runTest {
    viewModel(balances = emptyList()).uiState.test {
      assertEquals(AccountsListUiState.Empty, resolved(AccountsListUiState.Loading))
    }
  }

  @Test
  fun `an unfunded envelope is measured against its lifetime funding`() = runTest {
    val bank = account("Bank", AccountType.REAL)
    val food = account("Food", AccountType.BUDGET, parent = bank.id)
    val balances = listOf(
      AccountBalance(bank, Money(4_000_000)), // Rs 40,000
      AccountBalance(food, Money(950_000)),   // Rs 9,500 remaining
    )

    viewModel(balances, spend = mapOf(food.id to Money(650_000))).uiState.test {
      val content = resolved(AccountsListUiState.Loading) as AccountsListUiState.Content
      assertEquals(2, content.accountCount)
      assertEquals("Rs 40,000", content.realTotal)
      assertEquals("Rs 9,500", content.budgetTotal)
      assertEquals(1, content.real.size)

      val progress = content.budgets.single().progress!!
      assertEquals("Rs 6,500", progress.spent)
      // funded = remaining + all-time spend
      assertEquals(UiText.Res(R.string.budget_progress_lifetime, listOf("Rs 16,000")), progress.caption)
      assertEquals(0.40625f, progress.fraction)
    }
  }

  @Test
  fun `a monthly funding schedule makes the denominator this month's refill`() = runTest {
    val bank = account("Bank", AccountType.REAL)
    val food = account("Food", AccountType.BUDGET, parent = bank.id)
    val balances = listOf(
      AccountBalance(bank, Money(4_000_000)),
      // Rs 33,250 — an envelope carrying two months forward, which is exactly the
      // case that made the lifetime denominator read wrong.
      AccountBalance(food, Money(3_325_000)),
    )

    val vm = viewModel(
      balances,
      spend = mapOf(food.id to Money(5_475_000)),            // Rs 54,750 all time
      spendThisMonth = mapOf(food.id to Money(2_200_000)),   // Rs 22,000 this month
      schedules = listOf(allocation(to = food.id, amount = Money(2_800_000))), // Rs 28,000/mo
    )

    vm.uiState.test {
      val content = resolved(AccountsListUiState.Loading) as AccountsListUiState.Content
      val progress = content.budgets.single().progress!!
      assertEquals("Rs 22,000", progress.spent)
      assertEquals(UiText.Res(R.string.budget_progress_month, listOf("Rs 28,000")), progress.caption)
      assertEquals(2_200_000f / 2_800_000f, progress.fraction)
    }
  }

  private fun allocation(to: AccountId, amount: Money) = RecurringSchedule(
    id = RecurringScheduleId(Uuid.random()),
    name = "Food refill",
    amount = amount,
    fromAccountId = AccountId(Uuid.random()),
    toAccountId = to,
    categoryId = null,
    recurrence = Recurrence.Monthly(dayOfMonth = 1),
    nextDueAt = Instant.DISTANT_FUTURE,
    status = RecurringStatus.ACTIVE,
    pausedAt = null,
    createdAt = Instant.DISTANT_PAST,
    updatedAt = Instant.DISTANT_PAST,
  )
}
