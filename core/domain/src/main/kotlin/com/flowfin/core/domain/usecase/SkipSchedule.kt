package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.model.RecurringScheduleId
import kotlinx.datetime.TimeZone

/** Skips a firing: advances the due date by one period without recording anything. */
class SkipSchedule(
  private val recurring: RecurringRepository,
  private val zone: TimeZone,
) {
  suspend operator fun invoke(scheduleId: RecurringScheduleId): Either<RecurringError, Unit> = either {
    val schedule = recurring.getById(scheduleId) ?: raise(RecurringError.ScheduleNotFound(scheduleId))
    val nextDue = schedule.recurrence.nextDueAfter(schedule.nextDueAt, zone)
    recurring.advanceNextDue(schedule.id, nextDue).bind()
  }
}
