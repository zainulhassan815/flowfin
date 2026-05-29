package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.RecurringError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.RecurringDraft
import com.flowfin.core.model.RecurringSchedule
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

/**
 * Creates a recurring schedule. Validates the same income/expense shape a one-off
 * recording would, then stamps the first due date as the next occurrence after now.
 */
class CreateRecurringSchedule(
  private val accounts: AccountRepository,
  private val categories: CategoryRepository,
  private val recurring: RecurringRepository,
  private val clock: Clock,
  private val zone: TimeZone,
) {
  suspend operator fun invoke(draft: RecurringDraft): Either<RecurringError, RecurringSchedule> = either {
    ensure(draft.name.isNotBlank()) { RecurringError.NameBlank }
    ensure(draft.amount.isPositive) { RecurringError.AmountNotPositive }
    when (draft) {
      is RecurringDraft.Income -> {
        val to = activeAccount(draft.toAccount)
        ensure(to.isReal) { RecurringError.InvalidAccountForKind("income must land in a real account") }
        requireCategory(draft.category, CategoryScope.INCOME)
      }

      is RecurringDraft.Expense -> {
        activeAccount(draft.fromAccount) // spend from a real account or a budget — both allowed
        requireCategory(draft.category, CategoryScope.EXPENSE)
      }
    }
    val firstDueAt = draft.recurrence.nextDueAfter(clock.now(), zone)
    recurring.create(draft, firstDueAt).bind()
  }

  private suspend fun Raise<RecurringError>.activeAccount(id: AccountId): Account {
    val account = accounts.getById(id) ?: raise(RecurringError.AccountNotFound(id))
    ensure(!account.isArchived) { RecurringError.ArchivedAccount(id) }
    return account
  }

  private suspend fun Raise<RecurringError>.requireCategory(id: CategoryId, scope: CategoryScope) {
    val category = categories.getById(id) ?: raise(RecurringError.CategoryNotFound(id))
    ensure(category.scope == scope) { RecurringError.CategoryScopeMismatch }
  }
}
