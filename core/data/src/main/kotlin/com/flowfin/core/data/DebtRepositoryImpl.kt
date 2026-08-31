package com.flowfin.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import arrow.core.Either
import com.flowfin.core.database.FlowFinDatabase
import com.flowfin.core.domain.error.DebtError
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Debt
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.DebtStatus
import com.flowfin.core.model.DebtWithRemaining
import com.flowfin.core.model.Money
import com.flowfin.core.model.PersonId
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class DebtRepositoryImpl(
  private val db: FlowFinDatabase,
  private val ids: IdGenerator,
  private val clock: Clock,
  private val dispatcher: CoroutineDispatcher,
) : DebtRepository {

  private val debts get() = db.debtsQueries
  private val transactions get() = db.transactionsQueries

  override fun observeAll(): Flow<List<DebtWithRemaining>> =
    debts.selectAllWithRemaining().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toDebtWithRemaining() } }

  override fun observeByDirection(direction: DebtDirection): Flow<List<DebtWithRemaining>> =
    debts.selectByDirectionWithRemaining(direction).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toDebtWithRemaining() } }

  override fun observeWithRemaining(id: DebtId): Flow<DebtWithRemaining?> =
    debts.selectByIdWithRemaining(id).asFlow().mapToOneOrNull(dispatcher).map { it?.toDebtWithRemaining() }

  override suspend fun getById(id: DebtId): Debt? = withContext(dispatcher) {
    debts.selectById(id).executeAsOneOrNull()?.toModel()
  }

  override suspend fun open(
    direction: DebtDirection,
    personId: PersonId,
    accountId: AccountId?,
    amount: Money,
    currency: String,
    reason: String?,
    recordedAt: Instant,
  ): Either<DebtError, Debt> = withContext(dispatcher) {
    val now = clock.now()
    val txId = TransactionId(ids.next())
    val debtId = DebtId(ids.next())
    val borrowing = direction == DebtDirection.I_OWE
    Either.catch {
      // Both rows go in one transaction: the origin tx points at the debt and the
      // debt points back at the tx via deferred FKs, validated at commit. The tx
      // must be inserted first so the debt's currency trigger can read it.
      db.transaction {
        transactions.insert(
          id = txId,
          kind = if (borrowing) TransactionKind.DEBT_BORROW else TransactionKind.DEBT_LEND,
          from_account_id = if (borrowing) null else accountId,
          to_account_id = if (borrowing) accountId else null,
          amount_minor = amount.minorUnits,
          category_id = null,
          note = null,
          recorded_at = recordedAt,
          recurring_id = null,
          debt_id = debtId,
          created_at = now,
          updated_at = now,
        )
        debts.insert(
          id = debtId,
          person_id = personId,
          direction = direction,
          original_amount_minor = amount.minorUnits,
          currency = currency,
          reason = reason,
          status = DebtStatus.ACTIVE,
          origin_transaction_id = txId,
          created_at = now,
          updated_at = now,
        )
      }
      Debt(
        id = debtId,
        personId = personId,
        direction = direction,
        originalAmount = amount,
        currency = currency,
        reason = reason,
        status = DebtStatus.ACTIVE,
        originTransactionId = txId,
        createdAt = now,
        updatedAt = now,
        settledAt = null,
      )
    }.mapLeft { DebtError.Unexpected(it) }
  }

  override suspend fun recordRepayment(
    debt: Debt,
    accountId: AccountId?,
    amount: Money,
    note: String?,
    recordedAt: Instant,
  ): Either<DebtError, Unit> = withContext(dispatcher) {
    val now = clock.now()
    val repayingOut = debt.direction == DebtDirection.I_OWE
    Either.catch {
      transactions.insert(
        id = TransactionId(ids.next()),
        kind = if (repayingOut) TransactionKind.DEBT_REPAY_OUT else TransactionKind.DEBT_REPAY_IN,
        from_account_id = if (repayingOut) accountId else null,
        to_account_id = if (repayingOut) null else accountId,
        amount_minor = amount.minorUnits,
        category_id = null,
        note = note,
        recorded_at = recordedAt,
        recurring_id = null,
        debt_id = debt.id,
        created_at = now,
        updated_at = now,
      ).value
      Unit
    }.mapLeft { DebtError.Unexpected(it) }
  }

  override suspend fun markSettled(id: DebtId): Either<DebtError, Unit> = withContext(dispatcher) {
    val now = clock.now()
    Either.catch {
      debts.markSettled(settledAt = now, updatedAt = now, id = id).value
      Unit
    }.mapLeft { DebtError.Unexpected(it) }
  }

  override suspend fun reopen(id: DebtId): Either<DebtError, Unit> = withContext(dispatcher) {
    Either.catch {
      debts.reopen(updatedAt = clock.now(), id = id).value
      Unit
    }.mapLeft { DebtError.Unexpected(it) }
  }

  override suspend fun delete(id: DebtId): Either<DebtError, Unit> = withContext(dispatcher) {
    Either.catch {
      // Transactions first — `transactions.debt_id` has no cascade, and the debt
      // row can't go while they still reference it.
      db.transaction {
        transactions.deleteByDebt(id)
        debts.delete(id)
      }
      Unit
    }.mapLeft { DebtError.Unexpected(it) }
  }
}
