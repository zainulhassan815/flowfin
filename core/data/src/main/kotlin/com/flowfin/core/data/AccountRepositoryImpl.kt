package com.flowfin.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.database.AccountsQueries
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Money
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

internal class AccountRepositoryImpl(
  private val queries: AccountsQueries,
  private val ids: IdGenerator,
  private val clock: Clock,
  private val dispatcher: CoroutineDispatcher,
) : AccountRepository {

  override fun observeActiveAccounts(): Flow<List<Account>> =
    queries.selectAllActive().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeBalances(): Flow<List<AccountBalance>> =
    queries.balanceForAll().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toAccountBalance() } }

  override fun observeAccountsByType(type: AccountType): Flow<List<Account>> =
    queries.selectByType(type).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeBudgets(parent: AccountId): Flow<List<Account>> =
    queries.selectBudgetsForParent(parent).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeTotalBalance(): Flow<Money> =
    queries.totalBalance().asFlow().mapToOne(dispatcher).map { Money(it) }

  override suspend fun getById(id: AccountId): Account? = withContext(dispatcher) {
    queries.selectById(id).executeAsOneOrNull()?.toModel()
  }

  override suspend fun balanceOf(id: AccountId): Money? = withContext(dispatcher) {
    queries.balance(id).executeAsOneOrNull()?.let { Money(it) }
  }

  override suspend fun activeNameExists(name: String): Boolean = withContext(dispatcher) {
    queries.countActiveWithName(name).executeAsOne() > 0L
  }

  override suspend fun create(
    name: String,
    type: AccountType,
    currency: String,
    parentAccountId: AccountId?,
    openingBalance: Money,
    color: String?,
    icon: String?,
    displayOrder: Int,
  ): Either<AccountError, Account> = withContext(dispatcher) {
    either {
      ensure(queries.countActiveWithName(name).executeAsOne() == 0L) { AccountError.DuplicateName(name) }
      val now = clock.now()
      val id = AccountId(ids.next())
      Either.catch {
        queries.insert(
          id = id,
          name = name,
          type = type,
          currency = currency,
          parent_account_id = parentAccountId,
          opening_balance_minor = openingBalance.minorUnits,
          color = color,
          icon = icon,
          display_order = displayOrder.toLong(),
          created_at = now,
          updated_at = now,
        ).value
      }.mapLeft { AccountError.Unexpected(it) }.bind()
      Account(
        id = id,
        name = name,
        type = type,
        currency = currency,
        parentAccountId = parentAccountId,
        openingBalance = openingBalance,
        color = color,
        icon = icon,
        displayOrder = displayOrder,
        createdAt = now,
        updatedAt = now,
        archivedAt = null,
      )
    }
  }

  override suspend fun updateBasics(
    id: AccountId,
    name: String,
    color: String?,
    icon: String?,
    displayOrder: Int,
  ): Either<AccountError, Unit> = withContext(dispatcher) {
    Either.catch {
      queries.updateBasics(name, color, icon, displayOrder.toLong(), clock.now(), id).value
      Unit
    }.mapLeft { AccountError.Unexpected(it) }
  }

  override suspend fun archive(id: AccountId): Either<AccountError, Unit> = withContext(dispatcher) {
    val now = clock.now()
    Either.catch {
      queries.archive(archivedAt = now, updatedAt = now, id = id).value
      Unit
    }.mapLeft { AccountError.Unexpected(it) }
  }

  override suspend fun unarchive(id: AccountId): Either<AccountError, Unit> = withContext(dispatcher) {
    Either.catch {
      queries.unarchive(updatedAt = clock.now(), id = id).value
      Unit
    }.mapLeft { AccountError.Unexpected(it) }
  }
}
