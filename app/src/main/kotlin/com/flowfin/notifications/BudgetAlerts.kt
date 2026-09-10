package com.flowfin.notifications

import android.content.Context
import com.flowfin.core.model.BudgetPeriod
import com.flowfin.core.model.BudgetStatus
import com.flowfin.core.model.Money
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

/**
 * "Food is 82% spent this month", and the harder one after it.
 *
 * Two thresholds, and only one of them is a number. **Low** is configurable in
 * Settings and applies to every budget at once; per-budget would be a schema
 * column, and nothing has asked for four different answers yet. **Overspent** is
 * not a threshold at all — it is the envelope having gone below zero — so there
 * is nothing there to configure.
 *
 * They measure different things, deliberately. **Low** is about pace — how far
 * into this month's refill the spending is. **Overspent** is about the envelope
 * itself having gone negative, which is the app's existing designed state and the
 * word its screens already use. A budget carrying money forward can be past its
 * monthly refill without being overspent, and both of those are worth saying.
 *
 * Only one fires per budget per run: an overspent envelope is past the threshold
 * by definition, and saying so twice would be noise.
 */
internal fun Notifier.postBudgetAlerts(
  context: Context,
  budgets: List<BudgetStatus>,
  money: MoneyFormatter,
  log: AlertLog,
  today: LocalDate,
  /** The "running low" mark, as a whole percent of this month's refill. */
  threshold: Int,
) {
  // The funding period. Monthly is the only cadence a budget's refill can have,
  // so the month is the period, and an alert stands until it turns over.
  val period = "${today.year}-${today.monthNumber}"

  budgets.forEach { status ->
    val id = status.account.id.value.toString()
    when {
      status.isOverspent -> {
        if (!log.isNew("budget:$id:over", period)) return@forEach
        post(
          kind = NotificationKind.BUDGET_ALERT,
          tag = "budget",
          id = "$id:over".hashCode(),
          title = context.getString(R.string.notif_budget_over_title, status.account.name),
          body = context.getString(
            R.string.notif_budget_over_body,
            money.display(Money(-status.remaining.minorUnits)),
          ),
          target = NotificationTarget.ACCOUNT,
          targetId = id,
        )
      }

      // A lifetime denominator has no month to be a share of, so a budget without
      // a funding schedule only ever gets the overspent alert.
      status.period == BudgetPeriod.MONTH && status.fraction * 100 >= threshold -> {
        if (!log.isNew("budget:$id:low", period)) return@forEach
        post(
          kind = NotificationKind.BUDGET_ALERT,
          tag = "budget",
          id = "$id:low".hashCode(),
          title = context.getString(
            R.string.notif_budget_low_title,
            status.account.name,
            (status.fraction * 100).roundToInt(),
          ),
          body = context.getString(
            R.string.notif_budget_low_body,
            money.display(status.spent),
            money.display(status.funded),
          ),
          target = NotificationTarget.ACCOUNT,
          targetId = id,
        )
      }
    }
  }
}
