package com.flowfin.core.model

import kotlinx.datetime.Instant

/**
 * One row in the ledger. [kind] discriminates the event shape; which of
 * [fromAccountId] / [toAccountId] / [categoryId] / [debtId] are set follows
 * from the kind (enforced by the database's per-kind CHECKs).
 */
data class Transaction(
  val id: TransactionId,
  val kind: TransactionKind,
  val fromAccountId: AccountId?,
  val toAccountId: AccountId?,
  val amount: Money,
  val categoryId: CategoryId?,
  val note: String?,
  val recordedAt: Instant,
  val recurringId: RecurringScheduleId?,
  val debtId: DebtId?,
  val createdAt: Instant,
  val updatedAt: Instant,
)

/** An amount with the date it was recorded — the raw input to a trend chart. */
data class DatedAmount(
  val recordedAt: Instant,
  val amount: Money,
)

/**
 * The intent to record a transaction, one variant per non-debt [TransactionKind].
 * Each variant carries exactly the fields its kind allows, so illegal shapes
 * (an INCOME with a `from`, a TRANSFER with a category) can't be constructed.
 *
 * The id and `created_at` / `updated_at` stamps are assigned by the data layer;
 * [recordedAt] is the user-facing date and is supplied here.
 *
 * Debt kinds (DEBT_BORROW / LEND / REPAY_*) arrive with the debts aggregate.
 */
sealed interface TransactionDraft {
  val kind: TransactionKind
  val amount: Money
  val recordedAt: Instant

  data class Income(
    val to: AccountId,
    override val amount: Money,
    val category: CategoryId,
    val note: String?,
    override val recordedAt: Instant,
  ) : TransactionDraft {
    override val kind: TransactionKind get() = TransactionKind.INCOME
  }

  data class Expense(
    val from: AccountId,
    override val amount: Money,
    val category: CategoryId,
    val note: String?,
    override val recordedAt: Instant,
  ) : TransactionDraft {
    override val kind: TransactionKind get() = TransactionKind.EXPENSE
  }

  data class Transfer(
    val from: AccountId,
    val to: AccountId,
    override val amount: Money,
    val note: String?,
    override val recordedAt: Instant,
  ) : TransactionDraft {
    override val kind: TransactionKind get() = TransactionKind.TRANSFER
  }

  data class Allocation(
    val from: AccountId,
    val to: AccountId,
    override val amount: Money,
    override val recordedAt: Instant,
  ) : TransactionDraft {
    override val kind: TransactionKind get() = TransactionKind.ALLOCATION
  }

  data class Reallocation(
    val from: AccountId,
    val to: AccountId,
    override val amount: Money,
    override val recordedAt: Instant,
  ) : TransactionDraft {
    override val kind: TransactionKind get() = TransactionKind.REALLOCATION
  }
}
