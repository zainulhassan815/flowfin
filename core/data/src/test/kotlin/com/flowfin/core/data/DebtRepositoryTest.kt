package com.flowfin.core.data

import com.flowfin.core.domain.error.DebtError
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreatePerson
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordBorrow
import com.flowfin.core.domain.usecase.RecordLend
import com.flowfin.core.domain.usecase.RecordRepayment
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtStatus
import com.flowfin.core.model.Money
import com.flowfin.core.model.PersonId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

class DebtRepositoryTest {

  private val at = Instant.fromEpochMilliseconds(1_700_000_000_000)
  private val db = inMemoryDatabase()
  private val accounts = AccountRepositoryImpl(db.accountsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val persons: PersonRepository = PersonRepositoryImpl(db.personsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val debts = DebtRepositoryImpl(db, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)

  private val createReal = CreateRealAccount(accounts)
  private val createBudget = CreateBudget(accounts)
  private val createPerson = CreatePerson(persons)
  private val borrow = RecordBorrow(accounts, persons, debts)
  private val lend = RecordLend(accounts, persons, debts)
  private val repay = RecordRepayment(accounts, debts)

  @Test
  fun `borrowing writes the debt and its origin transaction atomically`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val ahmed = createPerson("Ahmed").rightOrFail()

    val debt = borrow(ahmed.id, bank.id, Money(800_000), reason = "rent", recordedAt = at).rightOrFail()

    // The debt committed pointing at its origin transaction...
    val stored = debts.observeByDirection(DebtDirection.I_OWE).first().single()
    assertEquals(debt.id, stored.debt.id)
    assertEquals(debt.originTransactionId, stored.debt.originTransactionId)
    assertEquals(Money(800_000), stored.remaining)
    // ...and that origin transaction committed too, raising the account balance.
    assertEquals(Money(1_800_000), accounts.balanceOf(bank.id))
  }

  @Test
  fun `lending lowers the funding account and records it as owed to me`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val sara = createPerson("Sara").rightOrFail()

    val debt = lend(sara.id, bank.id, Money(500_000), recordedAt = at).rightOrFail()

    assertEquals(DebtDirection.OWED_TO_ME, debt.direction)
    assertEquals(Money(500_000), accounts.balanceOf(bank.id))
  }

  @Test
  fun `repaying what I owe reduces the remaining and the account`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val ahmed = createPerson("Ahmed").rightOrFail()
    val debt = borrow(ahmed.id, bank.id, Money(800_000), recordedAt = at).rightOrFail()

    repay(debt.id, bank.id, Money(200_000), recordedAt = at).rightOrFail()

    assertEquals(Money(600_000), debts.observeByDirection(DebtDirection.I_OWE).first().single().remaining)
    assertEquals(Money(1_600_000), accounts.balanceOf(bank.id))
  }

  @Test
  fun `repaying money owed to me brings it back into the account`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val sara = createPerson("Sara").rightOrFail()
    val debt = lend(sara.id, bank.id, Money(500_000), recordedAt = at).rightOrFail()

    repay(debt.id, bank.id, Money(300_000), recordedAt = at).rightOrFail()

    assertEquals(Money(200_000), debts.observeByDirection(DebtDirection.OWED_TO_ME).first().single().remaining)
    assertEquals(Money(800_000), accounts.balanceOf(bank.id))
  }

  @Test
  fun `overpaying a debt is allowed and drives the remaining negative`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val ahmed = createPerson("Ahmed").rightOrFail()
    val debt = borrow(ahmed.id, bank.id, Money(800_000), recordedAt = at).rightOrFail()

    repay(debt.id, bank.id, Money(1_000_000), recordedAt = at).rightOrFail()

    assertEquals(Money(-200_000), debts.observeByDirection(DebtDirection.I_OWE).first().single().remaining)
  }

  @Test
  fun `borrowing into a budget is rejected`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val food = createBudget("Food", bank.id).rightOrFail()
    val ahmed = createPerson("Ahmed").rightOrFail()

    val result = borrow(ahmed.id, food.id, Money(100_000), recordedAt = at)

    assertEquals(DebtError.AccountNotReal, result.leftOrFail())
  }

  @Test
  fun `a repayment in a different currency is rejected`() = runTest {
    val bank = createReal("Bank", currency = "PKR", openingBalance = Money(1_000_000)).rightOrFail()
    val dollars = createReal("Dollars", currency = "USD").rightOrFail()
    val ahmed = createPerson("Ahmed").rightOrFail()
    val debt = borrow(ahmed.id, bank.id, Money(800_000), recordedAt = at).rightOrFail()

    val result = repay(debt.id, dollars.id, Money(100_000), recordedAt = at)

    assertEquals(DebtError.CurrencyMismatch, result.leftOrFail())
  }

  @Test
  fun `borrowing for an unknown person is rejected`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()

    val result = borrow(PersonId(Uuid.generateV7()), bank.id, Money(100_000), recordedAt = at)

    assertIs<DebtError.PersonNotFound>(result.leftOrFail())
  }

  @Test
  fun `a non-positive amount is rejected`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val ahmed = createPerson("Ahmed").rightOrFail()

    val result = borrow(ahmed.id, bank.id, Money(0), recordedAt = at)

    assertEquals(DebtError.AmountNotPositive, result.leftOrFail())
  }

  @Test
  fun `settling then reopening a debt flips its status`() = runTest {
    val bank = createReal("Bank", openingBalance = Money(1_000_000)).rightOrFail()
    val ahmed = createPerson("Ahmed").rightOrFail()
    val debt = borrow(ahmed.id, bank.id, Money(800_000), recordedAt = at).rightOrFail()

    debts.markSettled(debt.id).rightOrFail()
    assertEquals(DebtStatus.SETTLED, debts.getById(debt.id)?.status)

    debts.reopen(debt.id).rightOrFail()
    assertEquals(DebtStatus.ACTIVE, debts.getById(debt.id)?.status)
  }
}
