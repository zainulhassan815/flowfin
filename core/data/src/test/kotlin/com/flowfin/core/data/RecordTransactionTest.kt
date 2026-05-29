package com.flowfin.core.data

import com.flowfin.core.domain.error.TransactionError
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
import kotlin.test.assertIs

/**
 * The account-type rules per kind (a TRANSFER stays REAL→REAL, an ALLOCATION
 * targets a BUDGET, a REALLOCATION stays within one parent) are domain-only
 * invariants the database does not enforce, so the rejection cases here are the
 * real guard. The total deliberately sums REAL + BUDGET and a negative balance
 * is allowed (warn, don't block) per the quality bar.
 */
class RecordTransactionTest {

  private val at = Instant.fromEpochMilliseconds(1_700_000_000_000)
  private val db = inMemoryDatabase()
  private val accounts = AccountRepositoryImpl(db.accountsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val transactions = TransactionRepositoryImpl(db.transactionsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val categories = CategoryRepositoryImpl(db.categoriesQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val record = RecordTransaction(accounts, transactions, categories)
  private val createReal = CreateRealAccount(accounts)
  private val createBudget = CreateBudget(accounts)

  @Test
  fun `income raises the destination balance`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val salary = db.seedCategory(CategoryScope.INCOME, at)

    record(TransactionDraft.Income(bank.id, Money(5_000), salary, note = null, recordedAt = at)).rightOrFail()

    assertEquals(Money(5_000), accounts.balanceOf(bank.id))
  }

  @Test
  fun `a transfer moves money between two real accounts`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(10_000)).rightOrFail()
    val savings = createReal("Savings").rightOrFail()

    record(TransactionDraft.Transfer(bank.id, savings.id, Money(3_000), note = null, recordedAt = at)).rightOrFail()

    assertEquals(Money(7_000), accounts.balanceOf(bank.id))
    assertEquals(Money(3_000), accounts.balanceOf(savings.id))
  }

  @Test
  fun `an allocation moves money from a real account into its budget`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(10_000)).rightOrFail()
    val food = createBudget("Food", bank.id).rightOrFail()

    record(TransactionDraft.Allocation(bank.id, food.id, Money(2_000), recordedAt = at)).rightOrFail()

    assertEquals(Money(8_000), accounts.balanceOf(bank.id))
    assertEquals(Money(2_000), accounts.balanceOf(food.id))
  }

  @Test
  fun `the total counts both the real account and the budget funded from it`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(10_000)).rightOrFail()
    val food = createBudget("Food", bank.id).rightOrFail()
    record(TransactionDraft.Allocation(bank.id, food.id, Money(2_000), recordedAt = at)).rightOrFail()

    assertEquals(Money(10_000), accounts.observeTotalBalance().first())
  }

  @Test
  fun `spending past the balance is allowed and goes negative`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000)).rightOrFail()
    val groceries = db.seedCategory(CategoryScope.EXPENSE, at)

    record(TransactionDraft.Expense(bank.id, Money(1_500), groceries, note = null, recordedAt = at)).rightOrFail()

    assertEquals(Money(-500), accounts.balanceOf(bank.id))
  }

  @Test
  fun `a transfer into a budget is rejected`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(10_000)).rightOrFail()
    val food = createBudget("Food", bank.id).rightOrFail()

    val result = record(TransactionDraft.Transfer(bank.id, food.id, Money(100), note = null, recordedAt = at))

    assertIs<TransactionError.InvalidAccountForKind>(result.leftOrFail())
  }

  @Test
  fun `a transfer across currencies is rejected`() = runTest {
    val bank = createReal("Bank", currency = "PKR", openingBalance = Money(10_000)).rightOrFail()
    val dollars = createReal("Dollars", currency = "USD").rightOrFail()

    val result = record(TransactionDraft.Transfer(bank.id, dollars.id, Money(100), note = null, recordedAt = at))

    assertEquals(TransactionError.CurrencyMismatch, result.leftOrFail())
  }

  @Test
  fun `spending from an archived account is rejected`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(10_000)).rightOrFail()
    val groceries = db.seedCategory(CategoryScope.EXPENSE, at)
    accounts.archive(bank.id).rightOrFail()

    val result = record(TransactionDraft.Expense(bank.id, Money(100), groceries, note = null, recordedAt = at))

    assertIs<TransactionError.ArchivedAccount>(result.leftOrFail())
  }

  @Test
  fun `a reallocation across different parents is rejected`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val wallet = createReal("Wallet").rightOrFail()
    val food = createBudget("Food", bank.id).rightOrFail()
    val fun_ = createBudget("Fun", wallet.id).rightOrFail()

    val result = record(TransactionDraft.Reallocation(food.id, fun_.id, Money(100), recordedAt = at))

    assertIs<TransactionError.InvalidAccountForKind>(result.leftOrFail())
  }

  @Test
  fun `a non-positive amount is rejected`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val salary = db.seedCategory(CategoryScope.INCOME, at)

    val result = record(TransactionDraft.Income(bank.id, Money(0), salary, note = null, recordedAt = at))

    assertEquals(TransactionError.AmountNotPositive, result.leftOrFail())
  }

  @Test
  fun `income tagged with an expense-scope category is rejected`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val groceries = db.seedCategory(CategoryScope.EXPENSE, at)

    val result = record(TransactionDraft.Income(bank.id, Money(5_000), groceries, note = null, recordedAt = at))

    assertEquals(TransactionError.CategoryScopeMismatch, result.leftOrFail())
  }
}
