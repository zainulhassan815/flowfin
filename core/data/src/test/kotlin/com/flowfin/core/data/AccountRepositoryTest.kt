package com.flowfin.core.data

import app.cash.turbine.test
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.TransactionDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccountRepositoryTest {

  private val at = Instant.fromEpochMilliseconds(1_700_000_000_000)
  private val db = inMemoryDatabase()
  private val accounts = AccountRepositoryImpl(db.accountsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val transactions = TransactionRepositoryImpl(db.transactionsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val createReal = CreateRealAccount(accounts)
  private val createBudget = CreateBudget(accounts)

  @Test
  fun `a created account appears in balances at its opening balance`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000)).rightOrFail()

    val entry = accounts.observeBalances().first().single { it.account.id == bank.id }

    assertEquals(Money(1_000), entry.balance)
  }

  @Test
  fun `balances re-emit when a transaction changes a balance`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000)).rightOrFail()
    val salary = db.seedCategory(CategoryScope.INCOME, at)

    accounts.observeBalances().test {
      assertEquals(Money(1_000), awaitItem().single { it.account.id == bank.id }.balance)

      transactions.record(TransactionDraft.Income(bank.id, Money(500), salary, note = null, recordedAt = at)).rightOrFail()

      assertEquals(Money(1_500), awaitItem().single { it.account.id == bank.id }.balance)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `creating an account whose active name is taken is rejected`() = runTest {
    createReal("Bank").rightOrFail()

    val result = createReal("Bank")

    assertEquals(AccountError.DuplicateName("Bank"), result.leftOrFail())
  }

  @Test
  fun `a blank account name is rejected`() = runTest {
    val result = createReal("   ")

    assertEquals(AccountError.NameBlank, result.leftOrFail())
  }

  @Test
  fun `an archived account is hidden from balances`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000)).rightOrFail()

    accounts.archive(bank.id).rightOrFail()

    assertTrue(accounts.observeBalances().first().none { it.account.id == bank.id })
  }

  @Test
  fun `a budget inherits its real parent's currency`() = runTest {
    val bank = createReal("Bank", currency = "USD").rightOrFail()

    val food = createBudget("Food", bank.id).rightOrFail()

    assertEquals("USD", food.currency)
  }

  @Test
  fun `a budget under a non-real parent is rejected`() = runTest {
    val bank = createReal("Bank").rightOrFail()
    val food = createBudget("Food", bank.id).rightOrFail()

    val result = createBudget("Snacks", food.id)

    assertEquals(AccountError.ParentNotReal, result.leftOrFail())
  }
}
