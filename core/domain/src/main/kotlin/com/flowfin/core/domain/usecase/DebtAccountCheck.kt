package com.flowfin.core.domain.usecase

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.DebtError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId

/**
 * Debt money always moves to/from a real, active account — never a budget. This
 * is a domain-only rule the database doesn't enforce, shared by the borrow, lend
 * and repayment use cases.
 */
internal suspend fun Raise<DebtError>.requireRealActiveAccount(
  accounts: AccountRepository,
  id: AccountId,
): Account {
  val account = accounts.getById(id) ?: raise(DebtError.AccountNotFound(id))
  ensure(account.isReal) { DebtError.AccountNotReal }
  ensure(!account.isArchived) { DebtError.ArchivedAccount(id) }
  return account
}
