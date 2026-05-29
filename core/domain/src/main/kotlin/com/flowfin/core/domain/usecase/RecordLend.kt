package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.flowfin.core.domain.error.DebtError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Debt
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.Money
import com.flowfin.core.model.PersonId
import kotlinx.datetime.Instant

/** Lending money to a person: it leaves a real account, creating an OWED_TO_ME debt. */
class RecordLend(
  private val accounts: AccountRepository,
  private val persons: PersonRepository,
  private val debts: DebtRepository,
) {
  suspend operator fun invoke(
    personId: PersonId,
    fromAccount: AccountId,
    amount: Money,
    reason: String? = null,
    recordedAt: Instant,
  ): Either<DebtError, Debt> = either {
    ensure(amount.isPositive) { DebtError.AmountNotPositive }
    ensureNotNull(persons.getById(personId)) { DebtError.PersonNotFound(personId) }
    val account = accounts.getById(fromAccount) ?: raise(DebtError.AccountNotFound(fromAccount))
    ensure(account.isReal) { DebtError.AccountNotReal }
    ensure(!account.isArchived) { DebtError.ArchivedAccount(fromAccount) }
    debts.open(
      direction = DebtDirection.OWED_TO_ME,
      personId = personId,
      accountId = fromAccount,
      amount = amount,
      currency = account.currency,
      reason = reason,
      recordedAt = recordedAt,
    ).bind()
  }
}
