package com.flowfin.core.domain.usecase

import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.BudgetPeriod
import com.flowfin.core.model.BudgetStatus
import com.flowfin.core.model.Money
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Every budget envelope with the figures that describe it. The Accounts list
 * draws a card from each; the daily notification pass decides whether one has
 * crossed a threshold. Same numbers, one definition.
 *
 * Computed on read, like every other balance in the app — nothing here is stored.
 */
class ObserveBudgetStatus(
  private val accounts: AccountRepository,
  private val transactions: TransactionRepository,
  private val recurring: RecurringRepository,
  private val clock: Clock,
  private val zone: TimeZone,
) {

  operator fun invoke(): Flow<List<BudgetStatus>> = flow {
    // Fixed when collection starts rather than per emission: the boundary only
    // moves at midnight on the 1st, and every collector here is short-lived.
    val monthStart = clock.now().toLocalDateTime(zone).date.let { LocalDate(it.year, it.month, 1) }
      .atStartOfDayIn(zone)

    emitAll(
      combine(
        accounts.observeBalances(),
        transactions.observeExpenseByAccount(Instant.DISTANT_PAST),
        transactions.observeExpenseByAccount(monthStart),
        recurring.observeActive(),
      ) { balances, allTime, thisMonth, schedules ->
        val monthlyFunding = schedules.monthlyFundingByBudget()
        balances.filter { it.account.isBudget }.map { balance ->
          val id = balance.account.id
          val declared = monthlyFunding[id]
          if (declared != null) {
            BudgetStatus(
              account = balance.account,
              remaining = balance.balance,
              spent = thisMonth[id] ?: Money.ZERO,
              funded = declared,
              period = BudgetPeriod.MONTH,
            )
          } else {
            val spent = allTime[id] ?: Money.ZERO
            BudgetStatus(
              account = balance.account,
              remaining = balance.balance,
              spent = spent,
              funded = balance.balance + spent,
              period = BudgetPeriod.LIFETIME,
            )
          }
        }
      },
    )
  }
}

/**
 * What each budget is scheduled to receive in a month, summed over its funding
 * schedules.
 *
 * Only monthly ones count. A weekly or yearly allocation would need converting,
 * and a converted figure presented as "this month" is a different number wearing
 * the same label — such a budget keeps the lifetime picture instead. Nothing in
 * the app creates one today; Add Budget only offers a monthly refill.
 */
private fun List<com.flowfin.core.model.RecurringSchedule>.monthlyFundingByBudget(): Map<AccountId, Money> =
  filter { it.kind == RecurringKind.ALLOCATION && it.recurrence is Recurrence.Monthly }
    .mapNotNull { schedule -> schedule.toAccountId?.let { it to schedule.amount } }
    .groupingBy { it.first }
    .fold(Money.ZERO) { sum, (_, amount) -> sum + amount }
