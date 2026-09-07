package com.flowfin.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.domain.usecase.ObserveBudgetStatus
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The once-a-day pass. Reads the current state of the app, decides what the user
 * hasn't already been told, and posts it.
 *
 * Everything it needs is already computed on read, so it does no writing beyond
 * its own bookkeeping — nothing here moves money.
 */
class DailyNotificationWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

  private val settingsRepository: SettingsRepository by inject()
  private val recurring: RecurringRepository by inject()
  private val transactions: TransactionRepository by inject()
  private val budgetStatus: ObserveBudgetStatus by inject()
  private val money: MoneyFormatter by inject()
  private val notifier: Notifier by inject()

  override suspend fun doWork(): Result {
    // Nothing would be shown, so nothing is worth computing — and no alert should
    // be marked as sent when it wasn't.
    if (!notifier.enabled) return Result.success()

    val settings = settingsRepository.observe().first()
    val log = AlertLog(settings.alertsFired)
    val now = Clock.System.now()
    val zone = TimeZone.currentSystemDefault()
    val today = now.toLocalDateTime(zone).date

    if (settings.paymentAlertsEnabled) {
      notifier.postPaymentAlerts(
        context = applicationContext,
        pending = recurring.observePending(now).first(),
        money = money,
        log = log,
        zone = zone,
        today = today,
      )
    }

    if (settings.dailyReminderEnabled) {
      notifier.postDailyReminder(
        context = applicationContext,
        transactions = transactions,
        log = log,
        today = today,
        zone = zone,
      )
    }

    if (settings.budgetAlertsEnabled) {
      notifier.postBudgetAlerts(
        context = applicationContext,
        budgets = budgetStatus().first(),
        money = money,
        log = log,
        today = today,
      )
    }

    settingsRepository.update { it.copy(alertsFired = log.seen) }
    return Result.success()
  }
}

/**
 * Dedupe for alerts that would otherwise repeat: an overdue bill is overdue again
 * tomorrow, and a budget stays over its threshold for the rest of the month.
 *
 * Each subject is recorded against the occurrence it was last announced for — a
 * due date, a funding period — so the alert fires when that changes and not
 * before. Subjects not seen this run drop out of [seen], which is how the log
 * stays the size of the app's live state rather than growing forever.
 */
internal class AlertLog(private val previous: Map<String, String>) {

  private val current = mutableMapOf<String, String>()

  /** Records that [key] currently stands at [occurrence], and answers whether
   *  that is news — i.e. whether to actually post something. */
  fun isNew(key: String, occurrence: String): Boolean {
    current[key] = occurrence
    return previous[key] != occurrence
  }

  val seen: Map<String, String> get() = current
}
