package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.model.RecurringScheduleId
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

/**
 * Fires a due schedule: records its transaction (dated on the due date unless
 * overridden) and advances the due date by one period. Each call advances one
 * period from the current due date, so a late firing still lands on schedule.
 */
class FireSchedule(
  private val recurring: RecurringRepository,
  private val zone: TimeZone,
) {
  suspend operator fun invoke(
    scheduleId: RecurringScheduleId,
    recordedAt: Instant? = null,
  ): Either<RecurringError, Unit> = either {
    val schedule = recurring.getById(scheduleId) ?: raise(RecurringError.ScheduleNotFound(scheduleId))
    val nextDue = schedule.recurrence.nextDueAfter(schedule.nextDueAt, zone)
    recurring.fire(schedule, recordedAt ?: schedule.nextDueAt, nextDue).bind()
  }
}
