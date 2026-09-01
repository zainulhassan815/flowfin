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

  /**
   * What this schedule fires, read off the account columns — the same shape the
   * database's per-kind CHECK enforces on the transactions it produces:
   * income lands *in* one account, an expense pays *out of* one, and an allocation
   * moves between two (a real account into its budget).
   */
  val kind: RecurringKind
    get() = when {
      fromAccountId != null && toAccountId != null -> RecurringKind.ALLOCATION
      toAccountId != null -> RecurringKind.INCOME
      else -> RecurringKind.EXPENSE
    }

  /** Money actually leaving your accounts. An allocation isn't spending — it moves
   *  money into an envelope you still own — so it is deliberately not an expense. */
  val isExpense: Boolean get() = kind == RecurringKind.EXPENSE
}

/** The three transaction kinds a schedule can fire. */
enum class RecurringKind { INCOME, EXPENSE, ALLOCATION }

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

  /**
   * A budget's funding schedule: money moves from a real account into its budget
   * on a cadence. Carries no category — an allocation is a move, not spending.
   */
  data class Allocation(
    override val name: String,
    override val amount: Money,
    override val recurrence: Recurrence,
    val fromAccount: AccountId,
    val toBudget: AccountId,
  ) : RecurringDraft
}
