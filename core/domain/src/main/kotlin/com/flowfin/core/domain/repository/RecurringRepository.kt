package com.flowfin.core.domain.repository

import arrow.core.Either
import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.model.RecurringDraft
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.model.RecurringScheduleId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Recurring schedules. [fire] records a firing transaction (linked via
 * `recurringId`) and advances `nextDueAt` in one database transaction;
 * [advanceNextDue] moves the due date on without a firing (a skip).
 */
interface RecurringRepository {

  fun observeAll(): Flow<List<RecurringSchedule>>

  fun observeActive(): Flow<List<RecurringSchedule>>

  /** Active schedules whose next firing is due at or before [now]. */
  fun observePending(now: Instant): Flow<List<RecurringSchedule>>

  suspend fun getById(id: RecurringScheduleId): RecurringSchedule?

  suspend fun create(draft: RecurringDraft, firstDueAt: Instant): Either<RecurringError, RecurringSchedule>

  suspend fun fire(
    schedule: RecurringSchedule,
    recordedAt: Instant,
    nextDueAt: Instant,
  ): Either<RecurringError, Unit>

  suspend fun advanceNextDue(id: RecurringScheduleId, nextDueAt: Instant): Either<RecurringError, Unit>

  suspend fun pause(id: RecurringScheduleId): Either<RecurringError, Unit>

  suspend fun resume(id: RecurringScheduleId): Either<RecurringError, Unit>

  suspend fun delete(id: RecurringScheduleId): Either<RecurringError, Unit>
}
