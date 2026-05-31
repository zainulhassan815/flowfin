package com.flowfin.core.model

import kotlinx.datetime.Instant

/**
 * A money location. [AccountType.REAL] rows are real-money places (Bank, Cash);
 * [AccountType.BUDGET] rows are envelopes allocated from a real [parentAccountId].
 *
 * The balance is computed on read, not stored here — see [AccountBalance].
 */
data class Account(
  val id: AccountId,
  val name: String,
  val type: AccountType,
  val currency: String,
  val parentAccountId: AccountId?,
  val openingBalance: Money,
  val color: String?,
  val icon: String?,
  val displayOrder: Int,
  val createdAt: Instant,
  val updatedAt: Instant,
  val archivedAt: Instant?,
) {
  val isReal: Boolean get() = type == AccountType.REAL
  val isBudget: Boolean get() = type == AccountType.BUDGET
  val isArchived: Boolean get() = archivedAt != null
}

/** An [Account] paired with its computed balance (opening + Σin − Σout). */
data class AccountBalance(
  val account: Account,
  val balance: Money,
)

/**
 * One account's movement over a window — the In / Out / Net strip on Account
 * detail. [inflow] sums rows where the account received, [outflow] where it
 * paid; [net] (inflow − outflow) is exactly the balance change over the window.
 */
data class AccountFlow(
  val inflow: Money,
  val outflow: Money,
) {
  val net: Money get() = inflow - outflow
}
