package com.flowfin.core.domain.repository

import arrow.core.Either
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Accounts and their computed balances. Read streams are plain [Flow]s — a local
 * SQLite select doesn't produce a domain error. Writes return [Either] because
 * they can (duplicate name, bad parent, …).
 *
 * Ids and `created_at` / `updated_at` are assigned inside the implementation.
 */
interface AccountRepository {

  fun observeActiveAccounts(): Flow<List<Account>>

  /** Active accounts with balances — powers the Home grid and Accounts list. */
  fun observeBalances(): Flow<List<AccountBalance>>

  fun observeAccountsByType(type: AccountType): Flow<List<Account>>

  fun observeBudgets(parent: AccountId): Flow<List<Account>>

  /** Headline "Total": sum across all active accounts (REAL + BUDGET). */
  fun observeTotalBalance(): Flow<Money>

  suspend fun getById(id: AccountId): Account?

  suspend fun balanceOf(id: AccountId): Money?

  /** Whether a non-archived account already uses [name] (case-sensitive, like the index). */
  suspend fun activeNameExists(name: String): Boolean

  suspend fun create(
    name: String,
    type: AccountType,
    currency: String,
    parentAccountId: AccountId?,
    openingBalance: Money,
    color: String?,
    icon: String?,
    displayOrder: Int,
  ): Either<AccountError, Account>

  suspend fun updateBasics(
    id: AccountId,
    name: String,
    color: String?,
    icon: String?,
    displayOrder: Int,
  ): Either<AccountError, Unit>

  suspend fun archive(id: AccountId): Either<AccountError, Unit>

  suspend fun unarchive(id: AccountId): Either<AccountError, Unit>
}
