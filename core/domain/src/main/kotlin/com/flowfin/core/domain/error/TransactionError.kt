package com.flowfin.core.domain.error

import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId

/**
 * Failures when recording or mutating transactions. Per-aggregate sealed type.
 *
 * Every case here is produced by explicit validation in `RecordTransaction`
 * before the insert: account-type rules per kind (which the database does NOT
 * enforce — "the UI offers valid pickers"), currency, archived, self-loop,
 * amount, and category existence/scope. [Unexpected] is the coarse backstop for
 * any database constraint we didn't pre-check; we never classify it by parsing
 * the exception.
 */
sealed interface TransactionError {
  data object AmountNotPositive : TransactionError
  data class AccountNotFound(val id: AccountId) : TransactionError
  data class ArchivedAccount(val id: AccountId) : TransactionError
  data class InvalidAccountForKind(val reason: String) : TransactionError
  data object SelfTransfer : TransactionError
  data object CurrencyMismatch : TransactionError
  data class CategoryNotFound(val id: CategoryId) : TransactionError
  data object CategoryScopeMismatch : TransactionError

  data class Unexpected(val cause: Throwable) : TransactionError
}
