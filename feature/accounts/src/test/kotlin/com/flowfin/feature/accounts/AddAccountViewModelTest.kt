package com.flowfin.feature.accounts

import app.cash.turbine.test
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.model.Money
import com.flowfin.core.ui.MoneyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
