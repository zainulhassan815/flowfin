package com.flowfin.core.domain.error

import com.flowfin.core.model.AccountId

/**
 * Failures when creating or mutating accounts. Per-aggregate sealed type — there
 * is no app-wide error supertype.
 *
 * Every case here is produced by explicit validation (a blank name, a name that
 * already exists, a bad parent). [Unexpected] is the coarse backstop for any
 * database constraint failure we didn't pre-check — we never classify it by
 * parsing the exception.
 */
sealed interface AccountError {
  data object NameBlank : AccountError
  data class DuplicateName(val name: String) : AccountError

  /** A budget was created against a parent that isn't a REAL account. */
  data object ParentNotReal : AccountError
  data class ParentNotFound(val id: AccountId) : AccountError

  data class Unexpected(val cause: Throwable) : AccountError
}
