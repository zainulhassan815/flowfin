package com.flowfin.core.model

/**
 * A budget envelope measured against what funds it.
 *
 * The window depends on whether the budget has a funding schedule. One that
 * refills every month is measured against **this month** — [spent] since the
 * month began, [funded] the declared refill. One that doesn't has no period to
 * measure, so it keeps the lifetime picture: everything ever spent out of it,
 * against everything ever put in ([remaining] + [spent]).
 *
 * Scoping both halves to the month without a schedule would read worse than the
 * lifetime figure, not better: an envelope carrying money forward would show a
 * balance larger than its own denominator.
 */
data class BudgetStatus(
  val account: Account,
  val remaining: Money,
  val spent: Money,
  val funded: Money,
  val period: BudgetPeriod,
) {
  /** Spend as a share of funding, clamped — an overspent envelope shows a full
   *  bar rather than one that runs off the end. [isOverspent] carries the rest. */
  val fraction: Float
    get() = if (funded.isPositive) {
      (spent.minorUnits.toFloat() / funded.minorUnits).coerceIn(0f, 1f)
    } else {
      0f
    }

  /** The envelope has gone negative — deliberately allowed, and worth saying. */
  val isOverspent: Boolean get() = remaining.minorUnits < 0
}

/** What [BudgetStatus.spent] and [BudgetStatus.funded] are measured over. */
enum class BudgetPeriod { MONTH, LIFETIME }
