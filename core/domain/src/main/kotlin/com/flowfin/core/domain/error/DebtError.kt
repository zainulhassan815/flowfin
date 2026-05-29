package com.flowfin.core.domain.error

import com.flowfin.core.model.AccountId
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.PersonId

/**
 * Failures when opening or repaying debts. Like transactions, the account-type
 * rule (debts move money to/from REAL accounts, never budgets) is a domain-only
 * invariant surfaced here as [AccountNotReal]. Overpayment is NOT an error —
 * a repayment past the remaining is allowed (forgiveness / settling up).
 */
sealed interface DebtError {
  data object AmountNotPositive : DebtError
  data class PersonNotFound(val id: PersonId) : DebtError
  data class AccountNotFound(val id: AccountId) : DebtError
  data class ArchivedAccount(val id: AccountId) : DebtError
  data object AccountNotReal : DebtError
  data object CurrencyMismatch : DebtError
  data class DebtNotFound(val id: DebtId) : DebtError
  data class Unexpected(val cause: Throwable) : DebtError
}
