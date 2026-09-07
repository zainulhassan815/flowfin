package com.flowfin.notifications

import android.content.Context
import com.flowfin.core.model.RecurringKind
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

/**
 * "Netflix is 3 days late." Every schedule that has come due and hasn't been
 * fired, once per due date.
 *
 * One notification per schedule rather than a digest: each is separately
 * actionable and separately dismissible, and the dedupe means a run only posts
 * schedules the user hasn't already been told about — so several at once means
 * several genuinely came due on the same day.
 */
internal fun Notifier.postPaymentAlerts(
  context: Context,
  pending: List<RecurringSchedule>,
  money: MoneyFormatter,
  log: AlertLog,
  zone: TimeZone = TimeZone.currentSystemDefault(),
  today: LocalDate,
) {
  pending
    // A budget's funding schedule is a RecurringSchedule too, since FLO-34. It
    // moves money between two accounts the user owns, so calling it "due" would
    // be telling them they owe themselves rent.
    .filter { it.kind != RecurringKind.ALLOCATION }
    .forEach { schedule ->
      val due = schedule.nextDueAt.toLocalDateTime(zone).date
      if (!log.isNew("payment:${schedule.id.value}", due.toString())) return@forEach

      val daysLate = today.toEpochDays() - due.toEpochDays()
      val amount = money.display(schedule.amount)
      val (title, body) = if (daysLate <= 0) {
        context.getString(R.string.notif_payment_due_title, schedule.name) to
          context.getString(R.string.notif_payment_due_body, amount)
      } else {
        context.resources.getQuantityString(
          R.plurals.notif_payment_late_title,
          daysLate,
          schedule.name,
          daysLate,
        ) to context.getString(R.string.notif_payment_late_body, amount, due.short())
      }

      post(
        kind = NotificationKind.PAYMENT_ALERT,
        tag = "payment",
        id = schedule.id.value.hashCode(),
        title = title,
        body = body,
        target = NotificationTarget.RECURRING,
      )
    }
}

private val shortDate = DateTimeFormatter.ofPattern("d MMM")

/** "4 Sep" — the date without a year, which is only ever days ago. */
private fun LocalDate.short(): String = shortDate.format(toJavaLocalDate())
