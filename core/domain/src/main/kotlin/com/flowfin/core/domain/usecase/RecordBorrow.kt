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

/** Borrowing money from a person: it lands in a real account, creating an I_OWE debt. */
class RecordBorrow(
  private val accounts: AccountRepository,
  private val persons: PersonRepository,
  private val debts: DebtRepository,
) {
  suspend operator fun invoke(
    personId: PersonId,
    intoAccount: AccountId,
    amount: Money,
    reason: String? = null,
    recordedAt: Instant,
  ): Either<DebtError, Debt> = either {
    ensure(amount.isPositive) { DebtError.AmountNotPositive }
    ensureNotNull(persons.getById(personId)) { DebtError.PersonNotFound(personId) }
    val account = requireRealActiveAccount(accounts, intoAccount)
    debts.open(
      direction = DebtDirection.I_OWE,
      personId = personId,
      accountId = intoAccount,
      amount = amount,
      currency = account.currency,
      reason = reason,
      recordedAt = recordedAt,
    ).bind()
  }
}
