package com.flowfin.core.domain.repository

import arrow.core.Either
import com.flowfin.core.domain.error.DebtError
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Debt
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.DebtWithRemaining
import com.flowfin.core.model.Money
import com.flowfin.core.model.PersonId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Debts and their computed remaining. [open] writes the debt and its origin
 * transaction together in one database transaction (they reference each other
 * through deferred foreign keys, validated at commit); [recordRepayment] adds a
 * single repayment transaction linked to the debt.
 */
interface DebtRepository {

  fun observeAll(): Flow<List<DebtWithRemaining>>

  fun observeByDirection(direction: DebtDirection): Flow<List<DebtWithRemaining>>

  suspend fun getById(id: DebtId): Debt?

  /**
   * Opens a debt: inserts the origin transaction (DEBT_BORROW for I_OWE into the
   * account, DEBT_LEND for OWED_TO_ME out of it) and the debt row atomically.
   */
  suspend fun open(
    direction: DebtDirection,
    personId: PersonId,
    accountId: AccountId,
    amount: Money,
    currency: String,
    reason: String?,
    recordedAt: Instant,
  ): Either<DebtError, Debt>

  /** Records a repayment against [debt] (DEBT_REPAY_OUT for I_OWE, DEBT_REPAY_IN for OWED_TO_ME). */
  suspend fun recordRepayment(
    debt: Debt,
    accountId: AccountId,
    amount: Money,
    recordedAt: Instant,
  ): Either<DebtError, Unit>

  suspend fun markSettled(id: DebtId): Either<DebtError, Unit>

  suspend fun reopen(id: DebtId): Either<DebtError, Unit>
}
