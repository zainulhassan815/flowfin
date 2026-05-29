package com.flowfin.core.model

import kotlinx.datetime.Instant

/**
 * A template that fires income or expense transactions on a [recurrence]. Firings
 * are rows in `transactions` with `recurringId` set. "Pending" is computed
 * ([status] == ACTIVE and [nextDueAt] ≤ now), not stored.
 *
 * Read model: the income/expense distinction lives in which of [toAccountId] /
 * [fromAccountId] is set (mirroring [Transaction]). Use [RecurringDraft] to create.
 */
data class RecurringSchedule(
  val id: RecurringScheduleId,
  val name: String,
  val amount: Money,
  val fromAccountId: AccountId?,
  val toAccountId: AccountId?,
  val categoryId: CategoryId?,
  val recurrence: Recurrence,
  val nextDueAt: Instant,
  val status: RecurringStatus,
  val pausedAt: Instant?,
  val createdAt: Instant,
  val updatedAt: Instant,
) {
  val isActive: Boolean get() = status == RecurringStatus.ACTIVE
}

/**
 * The intent to create a schedule. Like [TransactionDraft], a variant per fireable
 * kind (income / expense — the only kinds the database allows a `recurringId` on),
 * carrying exactly the fields that kind needs.
 */
sealed interface RecurringDraft {
  val name: String
  val amount: Money
  val recurrence: Recurrence

  data class Income(
    override val name: String,
    override val amount: Money,
    override val recurrence: Recurrence,
    val toAccount: AccountId,
    val category: CategoryId,
  ) : RecurringDraft

  data class Expense(
    override val name: String,
    override val amount: Money,
    override val recurrence: Recurrence,
    val fromAccount: AccountId,
    val category: CategoryId,
  ) : RecurringDraft
}
