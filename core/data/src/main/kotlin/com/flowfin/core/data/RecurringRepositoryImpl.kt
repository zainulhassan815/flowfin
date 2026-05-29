package com.flowfin.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import arrow.core.Either
import com.flowfin.core.database.FlowFinDatabase
import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.model.RecurringDraft
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.model.RecurringStatus
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class RecurringRepositoryImpl(
  private val db: FlowFinDatabase,
  private val ids: IdGenerator,
  private val clock: Clock,
  private val dispatcher: CoroutineDispatcher,
) : RecurringRepository {

  private val schedules get() = db.recurringSchedulesQueries
  private val transactions get() = db.transactionsQueries

  override fun observeAll(): Flow<List<RecurringSchedule>> =
    schedules.selectAll().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeActive(): Flow<List<RecurringSchedule>> =
    schedules.selectActive().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observePending(now: Instant): Flow<List<RecurringSchedule>> =
    schedules.selectPending(now).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override suspend fun getById(id: RecurringScheduleId): RecurringSchedule? = withContext(dispatcher) {
    schedules.selectById(id).executeAsOneOrNull()?.toModel()
  }

  override suspend fun create(
    draft: RecurringDraft,
    firstDueAt: Instant,
  ): Either<RecurringError, RecurringSchedule> = withContext(dispatcher) {
    val now = clock.now()
    val id = RecurringScheduleId(ids.next())
    val fromAccount = (draft as? RecurringDraft.Expense)?.fromAccount
    val toAccount = (draft as? RecurringDraft.Income)?.toAccount
    val category = when (draft) {
      is RecurringDraft.Income -> draft.category
      is RecurringDraft.Expense -> draft.category
    }
    val recurrence = draft.recurrence
    Either.catch {
      schedules.insert(
        id = id,
        name = draft.name,
        amount_minor = draft.amount.minorUnits,
        from_account_id = fromAccount,
        to_account_id = toAccount,
        category_id = category,
        cadence = recurrence.cadence(),
        day_of_week = recurrence.dayOfWeekColumn,
        day_of_month = recurrence.dayOfMonthColumn,
        month_of_year = recurrence.monthOfYearColumn,
        next_due_at = firstDueAt,
        status = RecurringStatus.ACTIVE,
        created_at = now,
        updated_at = now,
      ).value
      RecurringSchedule(
        id = id,
        name = draft.name,
        amount = draft.amount,
        fromAccountId = fromAccount,
        toAccountId = toAccount,
        categoryId = category,
        recurrence = recurrence,
        nextDueAt = firstDueAt,
        status = RecurringStatus.ACTIVE,
        pausedAt = null,
        createdAt = now,
        updatedAt = now,
      )
    }.mapLeft { RecurringError.Unexpected(it) }
  }

  override suspend fun fire(
    schedule: RecurringSchedule,
    recordedAt: Instant,
    nextDueAt: Instant,
  ): Either<RecurringError, Unit> = withContext(dispatcher) {
    val now = clock.now()
    val income = schedule.toAccountId != null
    Either.catch {
      // One transaction: record the firing (linked via recurring_id) and advance
      // the due date together, so a schedule never fires without also advancing.
      db.transaction {
        transactions.insert(
          id = TransactionId(ids.next()),
          kind = if (income) TransactionKind.INCOME else TransactionKind.EXPENSE,
          from_account_id = schedule.fromAccountId,
          to_account_id = schedule.toAccountId,
          amount_minor = schedule.amount.minorUnits,
          category_id = schedule.categoryId,
          note = null,
          recorded_at = recordedAt,
          recurring_id = schedule.id,
          debt_id = null,
          created_at = now,
          updated_at = now,
        )
        schedules.advanceNextDue(nextDueAt = nextDueAt, updatedAt = now, id = schedule.id)
      }
      Unit
    }.mapLeft { RecurringError.Unexpected(it) }
  }

  override suspend fun advanceNextDue(
    id: RecurringScheduleId,
    nextDueAt: Instant,
  ): Either<RecurringError, Unit> = withContext(dispatcher) {
    Either.catch {
      schedules.advanceNextDue(nextDueAt = nextDueAt, updatedAt = clock.now(), id = id).value
      Unit
    }.mapLeft { RecurringError.Unexpected(it) }
  }

  override suspend fun pause(id: RecurringScheduleId): Either<RecurringError, Unit> = withContext(dispatcher) {
    val now = clock.now()
    Either.catch {
      schedules.pause(pausedAt = now, updatedAt = now, id = id).value
      Unit
    }.mapLeft { RecurringError.Unexpected(it) }
  }

  override suspend fun resume(id: RecurringScheduleId): Either<RecurringError, Unit> = withContext(dispatcher) {
    Either.catch {
      schedules.resume(updatedAt = clock.now(), id = id).value
      Unit
    }.mapLeft { RecurringError.Unexpected(it) }
  }

  override suspend fun delete(id: RecurringScheduleId): Either<RecurringError, Unit> = withContext(dispatcher) {
    Either.catch {
      schedules.delete(id).value
      Unit
    }.mapLeft { RecurringError.Unexpected(it) }
  }
}
