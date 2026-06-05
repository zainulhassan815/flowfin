package com.flowfin.core.data

import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `feedOfKinds` powers the Transactions list's All / In / Out / Transfers filter:
 * the newest-first ledger restricted to a set of kinds, served by the
 * (kind, recorded_at) index.
 */
class FeedOfKindsTest {

  private val t0 = Instant.fromEpochMilliseconds(1_700_000_000_000)
  private val t1 = Instant.fromEpochMilliseconds(1_700_000_001_000)
  private val t2 = Instant.fromEpochMilliseconds(1_700_000_002_000)

  private val db = inMemoryDatabase()
  private val accounts = AccountRepositoryImpl(db.accountsQueries, UuidV7Generator(), FixedClock(t0), Dispatchers.Unconfined)
  private val transactions = TransactionRepositoryImpl(db.transactionsQueries, UuidV7Generator(), FixedClock(t0), Dispatchers.Unconfined)
  private val categories = CategoryRepositoryImpl(db.categoriesQueries, UuidV7Generator(), FixedClock(t0), Dispatchers.Unconfined)
  private val record = RecordTransaction(accounts, transactions, categories)
  private val createReal = CreateRealAccount(accounts)

  @Test
  fun `restricts the feed to the given kinds, newest first`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(100_000)).rightOrFail()
    val cash = createReal("Cash", openingBalance = Money(10_000)).rightOrFail()
    val incomeCat = db.seedCategory(CategoryScope.INCOME, t0)
    val expenseCat = db.seedCategory(CategoryScope.EXPENSE, t0)

    record(TransactionDraft.Income(bank.id, Money(50_000), incomeCat, note = null, recordedAt = t0)).rightOrFail()
    record(TransactionDraft.Expense(bank.id, Money(3_000), expenseCat, note = null, recordedAt = t1)).rightOrFail()
    record(TransactionDraft.Transfer(bank.id, cash.id, Money(2_000), note = null, recordedAt = t2)).rightOrFail()

    // A single bucket's kinds returns only that kind.
    assertEquals(
      listOf(TransactionKind.EXPENSE),
      transactions.feedOfKinds(setOf(TransactionKind.EXPENSE), limit = 100).first().map { it.kind },
    )
    // Multiple kinds come back newest-first (transfer t2, expense t1, income t0).
    assertEquals(
      listOf(TransactionKind.TRANSFER, TransactionKind.EXPENSE, TransactionKind.INCOME),
      transactions.feedOfKinds(
        setOf(TransactionKind.INCOME, TransactionKind.EXPENSE, TransactionKind.TRANSFER),
        limit = 100,
      ).first().map { it.kind },
    )
  }

  @Test
  fun `an empty kind set yields no rows`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(10_000)).rightOrFail()
    val expenseCat = db.seedCategory(CategoryScope.EXPENSE, t0)
    record(TransactionDraft.Expense(bank.id, Money(500), expenseCat, note = null, recordedAt = t0)).rightOrFail()

    assertTrue(transactions.feedOfKinds(emptySet(), limit = 100).first().isEmpty())
  }
}
