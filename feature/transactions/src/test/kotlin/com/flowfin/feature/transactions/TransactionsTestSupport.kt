package com.flowfin.feature.transactions

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
import com.flowfin.core.model.CategoryTotal
import com.flowfin.core.model.DatedAmount
import com.flowfin.core.model.CategoryUsage
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionDraft
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

/** Shared fixtures and fakes for the transactions ViewModel tests. */

internal val EPOCH: Instant = Instant.fromEpochSeconds(0)

/** A clock frozen at a fixed instant, so time-derived state is deterministic. */
internal class FixedClock(private val instant: Instant) : Clock {
  override fun now(): Instant = instant
}

internal fun account(name: String, type: AccountType = AccountType.REAL): Account = Account(
  id = AccountId(Uuid.random()),
  name = name,
  type = type,
  currency = "PKR",
  parentAccountId = null,
  openingBalance = Money.ZERO,
  color = null,
  icon = null,
  displayOrder = 0,
  createdAt = EPOCH,
  updatedAt = EPOCH,
  archivedAt = null,
)

internal fun category(name: String, scope: CategoryScope, icon: String? = null, color: String? = null): Category =
  Category(
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

internal fun tx(
  kind: TransactionKind,
  amount: Money,
  recordedAt: Instant,
  from: AccountId? = null,
  to: AccountId? = null,
  category: CategoryId? = null,
  note: String? = null,
): Transaction = Transaction(
  id = TransactionId(Uuid.random()),
  kind = kind,
  fromAccountId = from,
  toAccountId = to,
  amount = amount,
  categoryId = category,
  note = note,
  recordedAt = recordedAt,
  recurringId = null,
  debtId = null,
  createdAt = recordedAt,
  updatedAt = recordedAt,
)

internal class FakeTransactionRepository(
  private val rows: List<Transaction> = emptyList(),
) : TransactionRepository {
  override fun feed(limit: Long): Flow<List<Transaction>> = flowOf(rows.take(limit.toInt()))
  override fun feedOfKinds(kinds: Set<TransactionKind>, limit: Long): Flow<List<Transaction>> =
    flowOf(rows.filter { it.kind in kinds }.take(limit.toInt()))
  override fun observeNetChange(startAt: Instant, endAt: Instant): Flow<Money> = throw NotImplementedError()
  override fun observeByDebt(debtId: DebtId): Flow<List<Transaction>> = throw NotImplementedError()
  override fun observeCategoryUsage(): Flow<Map<CategoryId, CategoryUsage>> = throw NotImplementedError()
  override fun observeAmountsOfKind(kind: TransactionKind, startAt: Instant, endAt: Instant): Flow<List<DatedAmount>> = throw NotImplementedError()
  override fun observeCategoryTotals(kind: TransactionKind, startAt: Instant, endAt: Instant): Flow<List<CategoryTotal>> = throw NotImplementedError()
  override fun observeByAccount(accountId: AccountId, limit: Long, offset: Long): Flow<List<Transaction>> = throw NotImplementedError()
  override fun observeFlow(accountId: AccountId, startAt: Instant, endAt: Instant): Flow<AccountFlow> = throw NotImplementedError()
  override fun observeExpenseByAccount(): Flow<Map<AccountId, Money>> = throw NotImplementedError()
  override suspend fun getById(id: TransactionId): Transaction? = throw NotImplementedError()
  override suspend fun record(draft: TransactionDraft): Either<TransactionError, Transaction> = throw NotImplementedError()
  override suspend fun updateContent(id: TransactionId, amount: Money, categoryId: CategoryId?, note: String?, recordedAt: Instant): Either<TransactionError, Unit> = throw NotImplementedError()
  override suspend fun delete(id: TransactionId): Either<TransactionError, Unit> = throw NotImplementedError()
}

internal class FakeAccountRepository(
  private val accounts: List<Account> = emptyList(),
) : AccountRepository {
  override fun observeActiveAccounts(): Flow<List<Account>> = flowOf(accounts)
  override fun observeBalances(): Flow<List<AccountBalance>> = throw NotImplementedError()
  override fun observeAccountsByType(type: AccountType): Flow<List<Account>> = throw NotImplementedError()
  override fun observeBudgets(parent: AccountId): Flow<List<Account>> = throw NotImplementedError()
  override fun observeTotalBalance(): Flow<Money> = throw NotImplementedError()
  override suspend fun getById(id: AccountId): Account? = throw NotImplementedError()
  override suspend fun balanceOf(id: AccountId): Money? = throw NotImplementedError()
  override suspend fun activeNameExists(name: String): Boolean = throw NotImplementedError()
  override suspend fun create(name: String, type: AccountType, currency: String, parentAccountId: AccountId?, openingBalance: Money, color: String?, icon: String?, displayOrder: Int): Either<AccountError, Account> = throw NotImplementedError()
  override suspend fun updateBasics(id: AccountId, name: String, color: String?, icon: String?, displayOrder: Int): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun archive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun unarchive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
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
