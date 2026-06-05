package com.flowfin.core.data

import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateCategory
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.CreateRecurringSchedule
import com.flowfin.core.domain.usecase.FireSchedule
import com.flowfin.core.domain.usecase.SkipSchedule
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RecurringRepositoryTest {

  private val zone = TimeZone.UTC
  private fun date(year: Int, month: Int, day: Int) = LocalDate(year, month, day).atStartOfDayIn(zone)

  private val clock = FixedClock(date(2024, 1, 10))
  private val db = inMemoryDatabase()
  private val accounts = AccountRepositoryImpl(db.accountsQueries, UuidV7Generator(), clock, Dispatchers.Unconfined)
  private val categories = CategoryRepositoryImpl(db.categoriesQueries, UuidV7Generator(), clock, Dispatchers.Unconfined)
  private val transactions = TransactionRepositoryImpl(db.transactionsQueries, UuidV7Generator(), clock, Dispatchers.Unconfined)
  private val recurring = RecurringRepositoryImpl(db, UuidV7Generator(), clock, Dispatchers.Unconfined)

  private val createReal = CreateRealAccount(accounts)
  private val createBudget = CreateBudget(accounts)
  private val createCategory = CreateCategory(categories)
  private val createSchedule = CreateRecurringSchedule(accounts, categories, recurring, clock, zone)
  private val fire = FireSchedule(recurring, zone)
  private val skip = SkipSchedule(recurring, zone)

  private suspend fun monthlySalaryInto(accountId: com.flowfin.core.model.AccountId, amount: Money = Money(15_000_000)) =
    createSchedule(
      RecurringDraft.Income(
        name = "Salary",
        amount = amount,
        recurrence = Recurrence.Monthly(dayOfMonth = 15),
        toAccount = accountId,
        category = createCategory("Salary", CategoryScope.INCOME).rightOrFail().id,
      ),
    ).rightOrFail()

  @Test
  fun `creating a schedule stamps the first due date and lists it active`() = runTest {
    val bank = createReal("Bank").rightOrFail()

    val schedule = monthlySalaryInto(bank.id)

    assertEquals(date(2024, 1, 15), schedule.nextDueAt)
    assertTrue(recurring.observeActive().first().any { it.id == schedule.id })
  }

  @Test
  fun `firing records a linked transaction, moves the balance and advances the due date`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val schedule = monthlySalaryInto(bank.id)

    fire(schedule.id).rightOrFail()

    assertEquals(Money(16_000_000), accounts.balanceOf(bank.id))
    val firing = transactions.feed(10).first().firstOrNull { it.recurringId == schedule.id }
    assertNotNull(firing)
    assertEquals(date(2024, 2, 15), recurring.getById(schedule.id)?.nextDueAt)
  }

  @Test
  fun `skipping advances the due date without recording anything`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val schedule = monthlySalaryInto(bank.id)

    skip(schedule.id).rightOrFail()

    assertEquals(Money(1_000_000), accounts.balanceOf(bank.id))
    assertEquals(date(2024, 2, 15), recurring.getById(schedule.id)?.nextDueAt)
    assertTrue(transactions.feed(10).first().none { it.recurringId == schedule.id })
  }

  @Test
  fun `pending tracks the due date and excludes paused schedules`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val schedule = monthlySalaryInto(bank.id) // due 2024-01-15

    assertTrue(recurring.observePending(date(2024, 1, 14)).first().none { it.id == schedule.id })
    assertTrue(recurring.observePending(date(2024, 1, 16)).first().any { it.id == schedule.id })

    recurring.pause(schedule.id).rightOrFail()
    assertTrue(recurring.observePending(date(2024, 1, 16)).first().none { it.id == schedule.id })
    assertTrue(recurring.observeActive().first().none { it.id == schedule.id })

    recurring.resume(schedule.id).rightOrFail()
    assertTrue(recurring.observeActive().first().any { it.id == schedule.id })
  }

  @Test
  fun `a recurring income into a budget is rejected`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val food = createBudget("Food", bank.id).rightOrFail()
    val salary = createCategory("Salary", CategoryScope.INCOME).rightOrFail()

    val result = createSchedule(
      RecurringDraft.Income("Salary", Money(100), Recurrence.Monthly(15), food.id, salary.id),
    )

    assertIs<RecurringError.InvalidAccountForKind>(result.leftOrFail())
  }

  @Test
  fun `a recurring expense with an income-scope category is rejected`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val salary = createCategory("Salary", CategoryScope.INCOME).rightOrFail()

    val result = createSchedule(
      RecurringDraft.Expense("Rent", Money(100), Recurrence.Monthly(1), bank.id, salary.id),
    )

    assertEquals(RecurringError.CategoryScopeMismatch, result.leftOrFail())
  }
}
