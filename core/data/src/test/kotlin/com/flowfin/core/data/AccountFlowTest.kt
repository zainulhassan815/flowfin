package com.flowfin.core.data

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

/**
 * `observeFlow` powers the In / Out / Net strip on Account detail: inflow sums
 * rows where the account received, outflow where it paid, both restricted to the
 * window — net is the balance change over it.
 */
class AccountFlowTest {

  private val monthStart = Instant.parse("2025-12-01T00:00:00Z")
  private val midMonth = Instant.parse("2025-12-15T00:00:00Z")
  private val monthEnd = Instant.parse("2026-01-01T00:00:00Z")
  private val beforeMonth = Instant.parse("2025-11-20T00:00:00Z")

  private val db = inMemoryDatabase()
  private val accounts = AccountRepositoryImpl(db.accountsQueries, UuidV7Generator(), FixedClock(midMonth), Dispatchers.Unconfined)
  private val transactions = TransactionRepositoryImpl(db.transactionsQueries, UuidV7Generator(), FixedClock(midMonth), Dispatchers.Unconfined)
  private val categories = CategoryRepositoryImpl(db.categoriesQueries, UuidV7Generator(), FixedClock(midMonth), Dispatchers.Unconfined)
  private val record = RecordTransaction(accounts, transactions, categories)
  private val createReal = CreateRealAccount(accounts)

  @Test
  fun `sums inflow and outflow for the account within the window`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(0)).rightOrFail()
    val cash = createReal("Cash", openingBalance = Money(0)).rightOrFail()
    val income = db.seedCategory(CategoryScope.INCOME, midMonth)

    record(TransactionDraft.Income(bank.id, Money(150_000), income, note = null, recordedAt = midMonth)).rightOrFail()
    record(TransactionDraft.Transfer(bank.id, cash.id, Money(5_000), note = null, recordedAt = midMonth)).rightOrFail()

    val bankFlow = transactions.observeFlow(bank.id, monthStart, monthEnd).first()
    assertEquals(Money(150_000), bankFlow.inflow)
    assertEquals(Money(5_000), bankFlow.outflow)
    assertEquals(Money(145_000), bankFlow.net)

    // The transfer landed in cash as inflow; cash paid out nothing.
    val cashFlow = transactions.observeFlow(cash.id, monthStart, monthEnd).first()
    assertEquals(Money(5_000), cashFlow.inflow)
    assertEquals(Money.ZERO, cashFlow.outflow)
  }

  @Test
  fun `excludes rows outside the window`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(0)).rightOrFail()
    val income = db.seedCategory(CategoryScope.INCOME, midMonth)

    // One inside the window, one the month before.
    record(TransactionDraft.Income(bank.id, Money(40_000), income, note = null, recordedAt = midMonth)).rightOrFail()
    record(TransactionDraft.Income(bank.id, Money(99_000), income, note = null, recordedAt = beforeMonth)).rightOrFail()

    val flow = transactions.observeFlow(bank.id, monthStart, monthEnd).first()
    assertEquals(Money(40_000), flow.inflow)
    assertEquals(Money.ZERO, flow.outflow)
  }
}
