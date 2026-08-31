package com.flowfin.core.model

import kotlinx.datetime.Instant

/**
 * A debt relationship with a [Person]. The money movements — the origin
 * borrow/lend and every repayment — live in `transactions` with `debtId` set;
 * [originTransactionId] pins the first one. [status] is user-set, so a debt can
 * be called settled regardless of whether the remaining is zero.
 */
data class Debt(
  val id: DebtId,
  val personId: PersonId,
  val direction: DebtDirection,
  val originalAmount: Money,
  val currency: String,
  val reason: String?,
  val status: DebtStatus,
  val originTransactionId: TransactionId,
  val createdAt: Instant,
  val updatedAt: Instant,
  val settledAt: Instant?,
) {
  val isSettled: Boolean get() = status == DebtStatus.SETTLED
}

/**
 * A [Debt] with its computed money position: [paid] is the sum of repayments,
 * [remaining] is original − paid (can go negative when overpaid — allowed).
 * [originRecordedAt] is the origin transaction's user-declared date (when the
 * debt was actually incurred) — distinct from `debt.createdAt`, the row's audit
 * timestamp, which can differ when a debt is backdated.
 */
data class DebtWithRemaining(
  val debt: Debt,
  val paid: Money,
  val remaining: Money,
  val originRecordedAt: Instant,
)
