package com.flowfin.feature.accounts

import app.cash.turbine.ReceiveTurbine
import arrow.core.Either
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.error.CategoryError
import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountFlow
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

/** Shared fixtures and fakes for the feature's ViewModel tests. */

internal val EPOCH: Instant = Instant.fromEpochSeconds(0)

/** A clock frozen at a fixed instant, so time-derived state is deterministic. */
internal class FixedClock(private val instant: Instant) : Clock {
  override fun now(): Instant = instant
}

internal fun account(
  name: String,
  type: AccountType = AccountType.REAL,
  parent: AccountId? = null,
  icon: String? = null,
  color: String? = null,
  createdAt: Instant = EPOCH,
): Account = Account(
  id = AccountId(Uuid.random()),
  name = name,
  type = type,
  currency = "PKR",
  parentAccountId = parent,
  openingBalance = Money.ZERO,
  color = color,
  icon = icon,
  displayOrder = 0,
  createdAt = createdAt,
  updatedAt = createdAt,
  archivedAt = null,
)

internal fun category(
  name: String,
  scope: CategoryScope,
  icon: String? = null,
  color: String? = null,
): Category = Category(
  id = CategoryId(Uuid.random()),
  name = name,
  scope = scope,
  isDefault = false,
  icon = icon,
  color = color,
  displayOrder = 0,
  createdAt = EPOCH,
  updatedAt = EPOCH,
  archivedAt = null,
)

/**
 * In-memory [AccountRepository]. Stateful where the Add-Account flow needs it
 * (create / name checks observe the same list); the balance stream for the read
 * screens is supplied directly. Unused methods throw.
 */
internal class FakeAccountRepository(
  accounts: List<Account> = emptyList(),
  private val balances: List<AccountBalance> = emptyList(),
) : AccountRepository {
  private val accountsFlow = MutableStateFlow(accounts)

  fun snapshot(): List<Account> = accountsFlow.value

  override fun observeActiveAccounts(): Flow<List<Account>> = accountsFlow.asStateFlow()
  override fun observeBalances(): Flow<List<AccountBalance>> = flowOf(balances)
  override suspend fun activeNameExists(name: String): Boolean = accountsFlow.value.any { it.name == name }
  override suspend fun getById(id: AccountId): Account? = accountsFlow.value.find { it.id == id }

  override suspend fun create(
    name: String,
    type: AccountType,
    currency: String,
    parentAccountId: AccountId?,
    openingBalance: Money,
    color: String?,
    icon: String?,
    displayOrder: Int,
  ): Either<AccountError, Account> {
    if (accountsFlow.value.any { it.name == name }) return Either.Left(AccountError.DuplicateName(name))
    val created = account(name, type, parentAccountId, icon, color).copy(
      currency = currency,
      openingBalance = openingBalance,
      displayOrder = displayOrder,
    )
    accountsFlow.update { it + created }
    return Either.Right(created)
  }

  override fun observeAccountsByType(type: AccountType): Flow<List<Account>> = throw NotImplementedError()
  override fun observeBudgets(parent: AccountId): Flow<List<Account>> = throw NotImplementedError()
  override fun observeTotalBalance(): Flow<Money> = throw NotImplementedError()
  override suspend fun balanceOf(id: AccountId): Money? = throw NotImplementedError()
  override suspend fun updateBasics(id: AccountId, name: String, color: String?, icon: String?, displayOrder: Int): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun archive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun unarchive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
}

internal class FakeTransactionRepository(
  private val expenseByAccount: Map<AccountId, Money> = emptyMap(),
  private val byAccount: List<Transaction> = emptyList(),
  private val flow: AccountFlow = AccountFlow(Money.ZERO, Money.ZERO),
) : TransactionRepository {
  override fun observeExpenseByAccount(): Flow<Map<AccountId, Money>> = flowOf(expenseByAccount)
  override fun observeByAccount(accountId: AccountId, limit: Long, offset: Long): Flow<List<Transaction>> = flowOf(byAccount)
  override fun observeFlow(accountId: AccountId, startAt: Instant, endAt: Instant): Flow<AccountFlow> = flowOf(flow)
  override fun recentFeed(limit: Long): Flow<List<Transaction>> = throw NotImplementedError()
  override fun observeNetChange(startAt: Instant, endAt: Instant): Flow<Money> = throw NotImplementedError()
  override suspend fun getById(id: TransactionId): Transaction? = throw NotImplementedError()
  override suspend fun record(draft: TransactionDraft): Either<TransactionError, Transaction> = throw NotImplementedError()
  override suspend fun updateContent(id: TransactionId, amount: Money, categoryId: CategoryId?, note: String?, recordedAt: Instant): Either<TransactionError, Unit> = throw NotImplementedError()
  override suspend fun delete(id: TransactionId): Either<TransactionError, Unit> = throw NotImplementedError()
}

internal class FakeCategoryRepository(
  private val categories: List<Category> = emptyList(),
) : CategoryRepository {
  override fun observeAll(): Flow<List<Category>> = flowOf(categories)
  override fun observeByScope(scope: CategoryScope): Flow<List<Category>> = throw NotImplementedError()
  override suspend fun ensureDefaultsSeeded(): Either<CategoryError, Unit> = throw NotImplementedError()
  override suspend fun getById(id: CategoryId): Category? = throw NotImplementedError()
  override suspend fun createCustom(name: String, scope: CategoryScope, icon: String?, color: String?, displayOrder: Int): Either<CategoryError, Category> = throw NotImplementedError()
  override suspend fun updateCustom(id: CategoryId, name: String, icon: String?, color: String?, displayOrder: Int): Either<CategoryError, Unit> = throw NotImplementedError()
  override suspend fun archive(id: CategoryId): Either<CategoryError, Unit> = throw NotImplementedError()
  override suspend fun unarchive(id: CategoryId): Either<CategoryError, Unit> = throw NotImplementedError()
}

/** stateIn starts at [loading]; step past it to the computed state. */
internal suspend fun <T> ReceiveTurbine<T>.resolved(loading: T): T {
  val first = awaitItem()
  return if (first == loading) awaitItem() else first
}
