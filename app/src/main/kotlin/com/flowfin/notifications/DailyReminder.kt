package com.flowfin.notifications

import android.content.Context
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.resources.R
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * The evening nudge, and only on a day that needs one.
 *
 * The PRD didn't say whether to fire on a day the user has already logged
 * something. Firing anyway is how an app gets muted, and it would make the copy
 * a lie — so the newest row's date decides it. That is also why the feed, not a
 * new query, answers the question: "was anything logged today" is "is the newest
 * transaction dated today".
 */
internal suspend fun Notifier.postDailyReminder(
  context: Context,
  transactions: TransactionRepository,
  log: AlertLog,
  today: LocalDate,
  zone: TimeZone = TimeZone.currentSystemDefault(),
) {
  val newest = transactions.feed(limit = 1).first().firstOrNull()
  if (newest != null && newest.recordedAt.toLocalDateTime(zone).date >= today) return
  if (!log.isNew("reminder", today.toString())) return

  post(
    kind = NotificationKind.DAILY_REMINDER,
    tag = "reminder",
    id = REMINDER_ID,
    title = context.getString(R.string.notif_reminder_title),
    body = context.getString(R.string.notif_reminder_body),
    // Straight to the form. The reminder asks for one thing; the tap should do it.
    target = NotificationTarget.ADD_TRANSACTION,
  )
}

private const val REMINDER_ID = 1
