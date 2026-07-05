package com.flowfin.feature.recurring

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

internal fun weekdayShort(isoDayNumber: Int): String = titleCase3(DayOfWeek(isoDayNumber).name)

internal fun weekdayFull(isoDayNumber: Int): String = titleCase(DayOfWeek(isoDayNumber).name)

internal fun monthShort(month: Month): String = titleCase3(month.name)

internal fun monthFull(month: Month): String = titleCase(month.name)

private fun titleCase(name: String): String = name.lowercase().replaceFirstChar { it.uppercase() }

private fun titleCase3(name: String): String = titleCase(name.take(3))

/** 1 → "1st", 22 → "22nd", 13 → "13th". */
internal fun ordinal(n: Int): String {
  val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
    1 -> "st"
    2 -> "nd"
    3 -> "rd"
    else -> "th"
  }
  return "$n$suffix"
}
