package com.flowfin.core.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * How often a schedule fires. Each variant carries exactly the fields its cadence
 * needs (1-based; ISO day-of-week with Monday = 1), validated on construction so
 * an out-of-range value can't exist.
 *
 * [nextDueAfter] returns the next firing strictly after [after], treating a firing
 * as the start of its day in [zone]. A day-of-month past the month's length is
 * clamped to the last day, so "the 31st" fires on Feb 28/29, Apr 30, and so on —
 * preserving the user's intent without skipping a month.
 */
sealed interface Recurrence {

  fun nextDueAfter(after: Instant, zone: TimeZone): Instant

  data class Weekly(val dayOfWeek: Int) : Recurrence {
    init { require(dayOfWeek in 1..7) { "dayOfWeek must be in 1..7, was $dayOfWeek" } }

    override fun nextDueAfter(after: Instant, zone: TimeZone): Instant {
      var date = after.toLocalDateTime(zone).date.plus(1, DateTimeUnit.DAY)
      while (date.dayOfWeek.isoDayNumber != dayOfWeek) {
        date = date.plus(1, DateTimeUnit.DAY)
      }
      return date.atStartOfDayIn(zone)
    }
  }

  data class Monthly(val dayOfMonth: Int) : Recurrence {
    init { require(dayOfMonth in 1..31) { "dayOfMonth must be in 1..31, was $dayOfMonth" } }

    override fun nextDueAfter(after: Instant, zone: TimeZone): Instant {
      val afterDate = after.toLocalDateTime(zone).date
      var date = clampedDate(afterDate.year, afterDate.monthNumber, dayOfMonth)
      if (date <= afterDate) {
        val nextMonth = LocalDate(afterDate.year, afterDate.monthNumber, 1).plus(1, DateTimeUnit.MONTH)
        date = clampedDate(nextMonth.year, nextMonth.monthNumber, dayOfMonth)
      }
      return date.atStartOfDayIn(zone)
    }
  }

  data class Yearly(val month: Int, val dayOfMonth: Int) : Recurrence {
    init {
      require(month in 1..12) { "month must be in 1..12, was $month" }
      require(dayOfMonth in 1..31) { "dayOfMonth must be in 1..31, was $dayOfMonth" }
    }

    override fun nextDueAfter(after: Instant, zone: TimeZone): Instant {
      val afterDate = after.toLocalDateTime(zone).date
      var date = clampedDate(afterDate.year, month, dayOfMonth)
      if (date <= afterDate) {
        date = clampedDate(afterDate.year + 1, month, dayOfMonth)
      }
      return date.atStartOfDayIn(zone)
    }
  }
}

private fun clampedDate(year: Int, month: Int, dayOfMonth: Int): LocalDate =
  LocalDate(year, month, minOf(dayOfMonth, daysInMonth(year, month)))

private fun daysInMonth(year: Int, month: Int): Int {
  val first = LocalDate(year, month, 1)
  return first.daysUntil(first.plus(1, DateTimeUnit.MONTH))
}
