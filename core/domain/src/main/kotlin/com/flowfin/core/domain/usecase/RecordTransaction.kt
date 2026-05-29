package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft

/**
 * Records a transaction, enforcing the domain-only invariants the database can't:
 * the account-type rules per kind. Currency / category-scope / archived rules are
 * also database triggers — checked here for a precise error, backstopped there.
 *
 * Validation runs before the insert; the database remains the final arbiter.
 */
class RecordTransaction(
  private val accounts: AccountRepository,
  private val transactions: TransactionRepository,
) {
  suspend operator fun invoke(draft: TransactionDraft): Either<TransactionError, Transaction> = either {
    ensure(draft.amount.isPositive) { TransactionError.AmountNotPositive }
    validate(draft)
    transactions.record(draft).bind()
  }

  private suspend fun Raise<TransactionError>.validate(draft: TransactionDraft) {
    when (draft) {
      is TransactionDraft.Income -> {
        val to = activeAccount(draft.to)
        ensure(to.isReal) { invalid("income must land in a real account") }
      }

      is TransactionDraft.Expense ->
        activeAccount(draft.from) // spend from a real account or a budget — both allowed

      is TransactionDraft.Transfer -> {
        ensure(draft.from != draft.to) { TransactionError.SelfTransfer }
        val from = activeAccount(draft.from)
        val to = activeAccount(draft.to)
        ensure(from.isReal && to.isReal) { invalid("transfer endpoints must both be real accounts") }
        ensure(from.currency == to.currency) { TransactionError.CurrencyMismatch }
      }

      is TransactionDraft.Allocation -> {
        ensure(draft.from != draft.to) { TransactionError.SelfTransfer }
        val from = activeAccount(draft.from)
        val to = activeAccount(draft.to)
        ensure(from.isReal) { invalid("allocation must come from a real account") }
        ensure(to.isBudget) { invalid("allocation must go to a budget") }
        ensure(from.currency == to.currency) { TransactionError.CurrencyMismatch }
      }

      is TransactionDraft.Reallocation -> {
        ensure(draft.from != draft.to) { TransactionError.SelfTransfer }
        val from = activeAccount(draft.from)
        val to = activeAccount(draft.to)
        ensure(from.isBudget && to.isBudget) { invalid("reallocation moves between budgets") }
        ensure(from.parentAccountId == to.parentAccountId) {
          invalid("reallocation must stay within one real parent")
        }
      }
    }
  }

  private suspend fun Raise<TransactionError>.activeAccount(id: AccountId): Account {
    val account = accounts.getById(id) ?: raise(TransactionError.AccountNotFound(id))
    ensure(!account.isArchived) { TransactionError.ArchivedAccount(id) }
    return account
  }

  private fun invalid(reason: String) = TransactionError.InvalidAccountForKind(reason)
}
