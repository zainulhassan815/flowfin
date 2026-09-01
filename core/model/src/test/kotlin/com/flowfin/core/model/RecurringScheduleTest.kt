package com.flowfin.core.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/**
 * A schedule's direction is read off its account columns rather than stored, so the
 * derivation has to stay total and match the database's per-kind CHECK on the rows
 * it fires. Allocation is the case that breaks a naive reading: it sets *both*
 * accounts, so "has a from account" means expense is wrong, and firing it as one
 * would insert a row the CHECK rejects.
 */
class RecurringScheduleTest {

  private val bank = AccountId(Uuid.generateV7())
  private val food = AccountId(Uuid.generateV7())
  private val salary = CategoryId(Uuid.generateV7())

  @Test
  fun `income lands in an account`() {
    val schedule = schedule(from = null, to = bank, category = salary)
    assertEquals(RecurringKind.INCOME, schedule.kind)
    assertFalse(schedule.isExpense)
  }

  @Test
  fun `an expense pays out of an account`() {
    val schedule = schedule(from = bank, to = null, category = salary)
    assertEquals(RecurringKind.EXPENSE, schedule.kind)
    assertTrue(schedule.isExpense)
  }

  @Test
  fun `both accounts set is a budget's funding allocation, not an expense`() {
    val schedule = schedule(from = bank, to = food, category = null)
    assertEquals(RecurringKind.ALLOCATION, schedule.kind)
    // The Recurring tab's monthly figure is what leaves your accounts. Funding an
    // envelope moves money you still hold, so counting it would inflate the total.
    assertFalse(schedule.isExpense)
  }

  private fun schedule(from: AccountId?, to: AccountId?, category: CategoryId?) = RecurringSchedule(
    id = RecurringScheduleId(Uuid.generateV7()),
    name = "Anything",
    amount = Money(100_000),
    fromAccountId = from,
    toAccountId = to,
    categoryId = category,
    recurrence = Recurrence.Monthly(dayOfMonth = 1),
    nextDueAt = Instant.parse("2026-09-01T00:00:00Z"),
    status = RecurringStatus.ACTIVE,
    pausedAt = null,
    createdAt = Instant.parse("2026-08-01T00:00:00Z"),
    updatedAt = Instant.parse("2026-08-01T00:00:00Z"),
  )
}
