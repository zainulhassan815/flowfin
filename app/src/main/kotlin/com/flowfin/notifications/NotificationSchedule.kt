package com.flowfin.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 * The one daily pass that drives every notification.
 *
 * WorkManager rather than an exact alarm: `setExactAndAllowWhileIdle` needs
 * `SCHEDULE_EXACT_ALARM`, which Play grants only to alarm and clock apps, and
 * FlowFin would not qualify. So the pass runs *around* the configured time, not
 * at it — which is why the PRD says "approximately".
 *
 * One worker for all three features, not three: they all want the same once-a-day
 * look at the same data, and three schedules would mean three wake-ups to read it.
 */
object NotificationSchedule {

  private const val WORK_NAME = "flowfin.daily-notifications"

  /** Bring the schedule in line with the user's settings. Safe to call repeatedly. */
  fun sync(context: Context, enabled: Boolean, at: LocalTime) {
    val work = WorkManager.getInstance(context)
    if (!enabled) {
      work.cancelUniqueWork(WORK_NAME)
      return
    }
    work.enqueueUniquePeriodicWork(
      WORK_NAME,
      // The delay is what carries the user's chosen time, so a changed time has
      // to re-enqueue rather than update an already-scheduled run in place.
      ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
      PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(untilNext(at).inWholeMinutes, TimeUnit.MINUTES)
        .build(),
    )
  }
}

/** How long until the next [at] on the wall clock — today's if it hasn't passed. */
internal fun untilNext(
  at: LocalTime,
  now: Instant = Clock.System.now(),
  zone: TimeZone = TimeZone.currentSystemDefault(),
): Duration {
  val today = now.toLocalDateTime(zone).date
  val todaysRun = today.atTime(at).toInstant(zone)
  val next = if (todaysRun > now) todaysRun else today.plus(1, DateTimeUnit.DAY).atTime(at).toInstant(zone)
  return next - now
}
