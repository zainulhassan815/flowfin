package com.flowfin.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import arrow.core.Either
import com.flowfin.core.database.TransactionsQueries
import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.AccountFlow
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryUsage
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

  override fun feed(limit: Long): Flow<List<Transaction>> =
    queries.feed(limit).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun feedOfKinds(kinds: Set<TransactionKind>, limit: Long): Flow<List<Transaction>> =
    // An empty set would generate `IN ()`, which SQLite rejects; no kinds means no rows.
    if (kinds.isEmpty()) flowOf(emptyList())
    else queries.feedOfKinds(kinds, limit).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeNetChange(startAt: Instant, endAt: Instant): Flow<Money> =
    queries.netChangeInRange(startAt, endAt).asFlow().mapToOne(dispatcher).map { Money(it) }

  override fun observeCategoryUsage(): Flow<Map<CategoryId, CategoryUsage>> =
    queries.usageByCategory().asFlow().mapToList(dispatcher).map { rows ->
      rows.mapNotNull { row ->
        row.category_id?.let { it to CategoryUsage(row.txn_count.toInt(), row.last_used_at) }
      }.toMap()
    }

  override fun observeByDebt(debtId: DebtId): Flow<List<Transaction>> =
    queries.byDebt(debtId).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeByAccount(accountId: AccountId, limit: Long, offset: Long): Flow<List<Transaction>> =
    queries.byAccount(accountId, limit, offset).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeFlow(accountId: AccountId, startAt: Instant, endAt: Instant): Flow<AccountFlow> =
    queries.flowByAccountInRange(accountId, startAt, endAt).asFlow().mapToOne(dispatcher).map {
      AccountFlow(inflow = Money(it.in_minor), outflow = Money(it.out_minor))
    }

  override fun observeExpenseByAccount(): Flow<Map<AccountId, Money>> =
    queries.expenseByAccount().asFlow().mapToList(dispatcher).map { rows ->
      rows.associate { it.account_id to Money(it.spent_minor) }
    }

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
  is TransactionDraft.Transfer -> null
  is TransactionDraft.Allocation -> null
  is TransactionDraft.Reallocation -> null
}

private fun TransactionDraft.note(): String? = when (this) {
  is TransactionDraft.Income -> note
  is TransactionDraft.Expense -> note
  is TransactionDraft.Transfer -> note
  is TransactionDraft.Allocation -> null
  is TransactionDraft.Reallocation -> null
}
