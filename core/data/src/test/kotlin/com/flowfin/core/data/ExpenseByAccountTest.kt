package com.flowfin.core.data

import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.TransactionDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * `observeExpenseByAccount` drives budget-envelope progress on the Accounts list:
 * the spend out of each envelope, with `funded = balance + spend`.
 */
class ExpenseByAccountTest {

  private val at = Instant.fromEpochMilliseconds(1_700_000_000_000)
  private val db = inMemoryDatabase()
  private val accounts = AccountRepositoryImpl(db.accountsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val transactions = TransactionRepositoryImpl(db.transactionsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val categories = CategoryRepositoryImpl(db.categoriesQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val record = RecordTransaction(accounts, transactions, categories)
  private val createReal = CreateRealAccount(accounts)
  private val createBudget = CreateBudget(accounts)

  @Test
  fun `sums expense outflow per account and omits accounts with none`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(16_000)).rightOrFail()
    val food = createBudget("Food", parentAccountId = bank.id).rightOrFail()
    val expense = db.seedCategory(CategoryScope.EXPENSE, at)

    record(TransactionDraft.Allocation(bank.id, food.id, Money(16_000), recordedAt = at)).rightOrFail()
    record(TransactionDraft.Expense(food.id, Money(6_500), expense, note = null, recordedAt = at)).rightOrFail()

    val spend = transactions.observeExpenseByAccount().first()

    assertEquals(Money(6_500), spend[food.id])
    // Bank only allocated out; an allocation isn't spend, so it's absent.
    assertNull(spend[bank.id])
    // Sanity: funded (16,000) = balance (9,500) + spend (6,500).
    assertEquals(Money(9_500), accounts.balanceOf(food.id))
  }

  @Test
  fun `accumulates multiple expenses from the same envelope`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(10_000)).rightOrFail()
    val food = createBudget("Food", parentAccountId = bank.id).rightOrFail()
    val expense = db.seedCategory(CategoryScope.EXPENSE, at)
    record(TransactionDraft.Allocation(bank.id, food.id, Money(10_000), recordedAt = at)).rightOrFail()

    record(TransactionDraft.Expense(food.id, Money(500), expense, note = null, recordedAt = at)).rightOrFail()
    record(TransactionDraft.Expense(food.id, Money(1_200), expense, note = null, recordedAt = at)).rightOrFail()

    assertEquals(Money(1_700), transactions.observeExpenseByAccount().first()[food.id])
  }
}
