package com.flowfin.core.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

private val karachi = TimeZone.of("Asia/Karachi")

/** 2026-09-10, 13:45 in Karachi. */
private val now = Instant.parse("2026-09-10T08:45:00Z")
private val fixed = object : Clock { override fun now() = now }

class RecordedAtTest {

  @Test
  fun `a row dated today is stamped with the current time`() {
    assertEquals(now, LocalDate(2026, 9, 10).recordedAt(fixed, karachi))
  }

  @Test
  fun `a back-dated row is stamped at noon in the user's zone`() {
    // 12:00 Karachi is 07:00Z — inside 9 September wherever it is read.
    assertEquals(
      Instant.parse("2026-09-09T07:00:00Z"),
      LocalDate(2026, 9, 9).recordedAt(fixed, karachi),
    )
  }

  @Test
  fun `two rows entered today keep the order they were entered in`() {
    val today = LocalDate(2026, 9, 10)
    var t = now
    val ticking = object : Clock {
      override fun now(): Instant = t
    }
    val first = today.recordedAt(ticking, karachi)
    t = now.plus(kotlin.time.Duration.parse("1m"))
    val second = today.recordedAt(ticking, karachi)
    assertEquals(true, second > first, "a later entry must carry a later stamp")
  }
}
