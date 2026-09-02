package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.model.RecurringScheduleId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

/**
 * Fires a due schedule: records its transaction and advances the due date by one
 * period.
 *
 * The transaction is dated **when it fired**, not when it fell due. Marking a
 * ten-day-late bill paid means the money leaves today, and balances here are
 * computed from transactions — backdating it to the due date would put money back
 * in the account for ten days it was never there. The schedule still advances one
 * period from its *due* date, so paying late doesn't drag the cadence with it.
 *
 * [recordedAt] overrides the stamp for a caller that knows better (a backfill).
 */
class FireSchedule(
  private val recurring: RecurringRepository,
  private val clock: Clock,
  private val zone: TimeZone,
) {
  suspend operator fun invoke(
    scheduleId: RecurringScheduleId,
    recordedAt: Instant? = null,
  ): Either<RecurringError, Unit> = either {
    val schedule = recurring.getById(scheduleId) ?: raise(RecurringError.ScheduleNotFound(scheduleId))
    val nextDue = schedule.recurrence.nextDueAfter(schedule.nextDueAt, zone)
    recurring.fire(schedule, recordedAt ?: clock.now(), nextDue).bind()
  }
}
