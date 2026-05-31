package com.flowfin.core.domain.repository

import arrow.core.Either
import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * The ledger. [record] is the low-level insert: it assigns the id and timestamps,
 * inserts the row, and maps any database constraint failure back to a typed
 * [TransactionError]. Domain-only invariants (account types per kind) are checked
 * by the `RecordTransaction` use case before this is called.
 */
interface TransactionRepository {

  /** Newest first, for the Home feed. */
  fun recentFeed(limit: Long): Flow<List<Transaction>>

  /**
   * Net change to the headline total over `[startAt, endAt)` — the signed sum of
   * external flows (income/borrow/repay-in positive, expense/lend/repay-out
   * negative). Powers Home's "this month" trend.
   */
  fun observeNetChange(startAt: Instant, endAt: Instant): Flow<Money>

  /** Every row touching one account, for Account detail. */
  fun observeByAccount(accountId: AccountId, limit: Long, offset: Long): Flow<List<Transaction>>

  /**
   * Cumulative expense outflow per account, keyed by id. Drives budget-envelope
   * progress on the Accounts list: a budget's `spent`, with `funded = balance + spent`.
   * Accounts with no expenses are absent from the map.
   */
  fun observeExpenseByAccount(): Flow<Map<AccountId, Money>>

  suspend fun getById(id: TransactionId): Transaction?

  suspend fun record(draft: TransactionDraft): Either<TransactionError, Transaction>

  suspend fun updateContent(
    id: TransactionId,
    amount: Money,
    categoryId: CategoryId?,
    note: String?,
    recordedAt: Instant,
  ): Either<TransactionError, Unit>

  suspend fun delete(id: TransactionId): Either<TransactionError, Unit>
}
