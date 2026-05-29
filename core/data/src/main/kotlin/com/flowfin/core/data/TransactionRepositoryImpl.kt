package com.flowfin.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import arrow.core.Either
import com.flowfin.core.database.TransactionsQueries
import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class TransactionRepositoryImpl(
  private val queries: TransactionsQueries,
  private val ids: IdGenerator,
  private val clock: Clock,
  private val dispatcher: CoroutineDispatcher,
) : TransactionRepository {

  override fun recentFeed(limit: Long): Flow<List<Transaction>> =
    queries.recentFeed(limit).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeByAccount(accountId: AccountId, limit: Long, offset: Long): Flow<List<Transaction>> =
    queries.byAccount(accountId, limit, offset).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override suspend fun getById(id: TransactionId): Transaction? = withContext(dispatcher) {
    queries.selectById(id).executeAsOneOrNull()?.toModel()
  }

  override suspend fun record(draft: TransactionDraft): Either<TransactionError, Transaction> = withContext(dispatcher) {
    val now = clock.now()
    val id = TransactionId(ids.next())
    val from = draft.fromAccountId()
    val to = draft.toAccountId()
    val category = draft.categoryId()
    val note = draft.note()
    Either.catch {
      queries.insert(
        id = id,
        kind = draft.kind,
        from_account_id = from,
        to_account_id = to,
        amount_minor = draft.amount.minorUnits,
        category_id = category,
        note = note,
        recorded_at = draft.recordedAt,
        recurring_id = null,
        debt_id = null,
        created_at = now,
        updated_at = now,
      ).value
      Transaction(
        id = id,
        kind = draft.kind,
        fromAccountId = from,
        toAccountId = to,
        amount = draft.amount,
        categoryId = category,
        note = note,
        recordedAt = draft.recordedAt,
        recurringId = null,
        debtId = null,
        createdAt = now,
        updatedAt = now,
      )
    }.mapLeft { TransactionError.Unexpected(it) }
  }

  override suspend fun updateContent(
    id: TransactionId,
    amount: Money,
    categoryId: CategoryId?,
    note: String?,
    recordedAt: Instant,
  ): Either<TransactionError, Unit> = withContext(dispatcher) {
    Either.catch {
      queries.updateContent(amount.minorUnits, categoryId, note, recordedAt, clock.now(), id).value
      Unit
    }.mapLeft { TransactionError.Unexpected(it) }
  }

  override suspend fun delete(id: TransactionId): Either<TransactionError, Unit> = withContext(dispatcher) {
    Either.catch {
      queries.delete(id).value
      Unit
    }.mapLeft { TransactionError.Unexpected(it) }
  }
}

private fun TransactionDraft.fromAccountId(): AccountId? = when (this) {
  is TransactionDraft.Income -> null
  is TransactionDraft.Expense -> from
  is TransactionDraft.Transfer -> from
  is TransactionDraft.Allocation -> from
  is TransactionDraft.Reallocation -> from
}

private fun TransactionDraft.toAccountId(): AccountId? = when (this) {
  is TransactionDraft.Income -> to
  is TransactionDraft.Expense -> null
  is TransactionDraft.Transfer -> to
  is TransactionDraft.Allocation -> to
  is TransactionDraft.Reallocation -> to
}

private fun TransactionDraft.categoryId(): CategoryId? = when (this) {
  is TransactionDraft.Income -> category
  is TransactionDraft.Expense -> category
  else -> null
}

private fun TransactionDraft.note(): String? = when (this) {
  is TransactionDraft.Income -> note
  is TransactionDraft.Expense -> note
  is TransactionDraft.Transfer -> note
  else -> null
}
