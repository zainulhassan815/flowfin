package com.flowfin.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The cadence calculator is the only real date math in the app. These pin the
 * tricky cases: strictly-after semantics, month rollover, and clamping a day past
 * the month's length (the "due on the 31st" / Feb-29 cases).
 */
class RecurrenceTest {

  private val utc = TimeZone.UTC
  private fun date(year: Int, month: Int, day: Int) = LocalDate(year, month, day).atStartOfDayIn(utc)

  @Test
  fun `weekly finds the next matching weekday strictly after`() {
    // 2024-01-10 is a Wednesday; next Monday is the 15th.
    assertEquals(date(2024, 1, 15), Recurrence.Weekly(dayOfWeek = 1).nextDueAfter(date(2024, 1, 10), utc))
  }

  @Test
  fun `weekly on the same weekday advances a full week`() {
    // 2024-01-15 is a Monday; strictly after lands on the next Monday.
    assertEquals(date(2024, 1, 22), Recurrence.Weekly(dayOfWeek = 1).nextDueAfter(date(2024, 1, 15), utc))
  }

  @Test
  fun `monthly returns this month while the day is still ahead`() {
    assertEquals(date(2024, 1, 15), Recurrence.Monthly(dayOfMonth = 15).nextDueAfter(date(2024, 1, 10), utc))
  }

  @Test
  fun `monthly rolls to next month once the day has passed`() {
    assertEquals(date(2024, 2, 15), Recurrence.Monthly(dayOfMonth = 15).nextDueAfter(date(2024, 1, 20), utc))
  }

  @Test
  fun `monthly clamps the 31st to the last day of a short month`() {
    assertEquals(date(2023, 2, 28), Recurrence.Monthly(dayOfMonth = 31).nextDueAfter(date(2023, 1, 31), utc))
  }

  @Test
  fun `monthly clamps the 31st to Feb 29 in a leap year`() {
    assertEquals(date(2024, 2, 29), Recurrence.Monthly(dayOfMonth = 31).nextDueAfter(date(2024, 2, 1), utc))
  }

  @Test
  fun `yearly clamps Feb 29 to Feb 28 in a non-leap year`() {
    assertEquals(date(2023, 2, 28), Recurrence.Yearly(month = 2, dayOfMonth = 29).nextDueAfter(date(2023, 1, 1), utc))
  }

  @Test
  fun `yearly hits Feb 29 in a leap year`() {
    assertEquals(date(2024, 2, 29), Recurrence.Yearly(month = 2, dayOfMonth = 29).nextDueAfter(date(2024, 1, 1), utc))
  }

  @Test
  fun `yearly rolls to next year once the date has passed`() {
    assertEquals(date(2025, 1, 1), Recurrence.Yearly(month = 1, dayOfMonth = 1).nextDueAfter(date(2024, 6, 1), utc))
  }

  @Test
  fun `an out-of-range day or month is rejected on construction`() {
    assertFailsWith<IllegalArgumentException> { Recurrence.Weekly(dayOfWeek = 8) }
    assertFailsWith<IllegalArgumentException> { Recurrence.Monthly(dayOfMonth = 32) }
    assertFailsWith<IllegalArgumentException> { Recurrence.Yearly(month = 13, dayOfMonth = 1) }
  }
}
