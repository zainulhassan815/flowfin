package com.flowfin.feature.accounts

import app.cash.turbine.test
import arrow.core.Either
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Money
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class AddAccountViewModelTest {

  @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())

  @AfterTest fun tearDown() = Dispatchers.resetMain()

  private fun viewModel(repo: FakeAccountRepository) =
    AddAccountViewModel(CreateRealAccount(repo), repo, MoneyFormatter())

  @Test
  fun `name starts blank so save is disabled`() = runTest {
    viewModel(FakeAccountRepository()).uiState.test {
      val state = awaitItem()
      assertEquals(NameError.Blank, state.nameError)
      assertFalse(state.canSave)
    }
  }

  @Test
  fun `a fresh name clears the error and enables save`() = runTest {
    val vm = viewModel(FakeAccountRepository())
    vm.uiState.test {
      awaitItem() // initial, blank
      vm.onNameChange("HBL Savings")
      val state = awaitItem()
      assertNull(state.nameError)
      assertTrue(state.canSave)
    }
  }

  @Test
  fun `a name already in use is flagged taken`() = runTest {
    val vm = viewModel(FakeAccountRepository(listOf(account("Cash"))))
    vm.uiState.test {
      awaitItem() // initial, blank
      vm.onNameChange("Cash")
      assertEquals(NameError.Taken, awaitItem().nameError)
    }
  }

  @Test
  fun `saving a valid account creates it with the kind's icon and colour, then navigates back`() = runTest {
    val repo = FakeAccountRepository()
    val vm = viewModel(repo)
    vm.onNameChange("Cash")
    vm.onSelectKind(AccountKind.Cash)
    vm.onBalanceChange("12.50")

    vm.effects.test {
      vm.save()
      assertEquals(AddAccountEffect.NavigateBack, awaitItem())
    }

    val created = repo.snapshot().single()
    assertEquals("Cash", created.name)
    assertEquals("wallet", created.icon)
    assertEquals("cash", created.color)
    assertEquals(Money(1_250), created.openingBalance)
  }

  @Test
  fun `saving a duplicate name reports a message instead of navigating`() = runTest {
    val vm = viewModel(FakeAccountRepository(listOf(account("Cash"))))
    vm.onNameChange("Cash")

    vm.effects.test {
      vm.save()
      assertTrue(awaitItem() is AddAccountEffect.ShowMessage)
    }
  }
}

private val EPOCH = Instant.fromEpochSeconds(0)

private fun account(name: String): Account = Account(
  id = AccountId(Uuid.random()),
  name = name,
  type = AccountType.REAL,
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

/** Minimal in-memory [AccountRepository] — only the methods this VM/use-case touch. */
private class FakeAccountRepository(initial: List<Account> = emptyList()) : AccountRepository {
  private val accounts = MutableStateFlow(initial)

  fun snapshot(): List<Account> = accounts.value

  override fun observeActiveAccounts(): Flow<List<Account>> = accounts.asStateFlow()

  override suspend fun activeNameExists(name: String): Boolean = accounts.value.any { it.name == name }

  override suspend fun getById(id: AccountId): Account? = accounts.value.find { it.id == id }

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
    if (accounts.value.any { it.name == name }) return Either.Left(AccountError.DuplicateName(name))
    val created = Account(
      id = AccountId(Uuid.random()),
      name = name,
      type = type,
      currency = currency,
      parentAccountId = parentAccountId,
      openingBalance = openingBalance,
      color = color,
      icon = icon,
      displayOrder = displayOrder,
      createdAt = EPOCH,
      updatedAt = EPOCH,
      archivedAt = null,
    )
    accounts.update { it + created }
    return Either.Right(created)
  }

  override fun observeBalances(): Flow<List<AccountBalance>> = throw NotImplementedError()
  override fun observeAccountsByType(type: AccountType): Flow<List<Account>> = throw NotImplementedError()
  override fun observeBudgets(parent: AccountId): Flow<List<Account>> = throw NotImplementedError()
  override fun observeTotalBalance(): Flow<Money> = throw NotImplementedError()
  override suspend fun balanceOf(id: AccountId): Money? = throw NotImplementedError()
  override suspend fun updateBasics(id: AccountId, name: String, color: String?, icon: String?, displayOrder: Int): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun archive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
  override suspend fun unarchive(id: AccountId): Either<AccountError, Unit> = throw NotImplementedError()
}
