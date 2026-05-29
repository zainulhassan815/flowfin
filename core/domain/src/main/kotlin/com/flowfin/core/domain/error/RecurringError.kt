package com.flowfin.core.domain.error

import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.RecurringScheduleId

/**
 * Failures when creating or firing recurring schedules. A schedule fires income or
 * expense, so creation validates the same account-type and category-scope rules as
 * recording one. [Unexpected] is the coarse backstop for an unforeseen DB failure.
 */
sealed interface RecurringError {
  data object NameBlank : RecurringError
  data object AmountNotPositive : RecurringError
  data class AccountNotFound(val id: AccountId) : RecurringError
  data class ArchivedAccount(val id: AccountId) : RecurringError
  data class InvalidAccountForKind(val reason: String) : RecurringError
  data class CategoryNotFound(val id: CategoryId) : RecurringError
  data object CategoryScopeMismatch : RecurringError
  data class ScheduleNotFound(val id: RecurringScheduleId) : RecurringError
  data class Unexpected(val cause: Throwable) : RecurringError
}
