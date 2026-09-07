package com.flowfin.notifications

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val karachi = TimeZone.of("Asia/Karachi")

class UntilNextTest {

  @Test
  fun `waits until this evening when the time is still ahead`() {
    // 09:00 Karachi (04:00Z), reminder at 20:00 → 11 hours off.
    val now = Instant.parse("2026-09-07T04:00:00Z")
    assertEquals(11.hours, untilNext(LocalTime(20, 0), now, karachi))
  }

  @Test
  fun `rolls to tomorrow once the time has passed`() {
    // 21:30 Karachi (16:30Z), reminder at 20:00 → 22.5 hours, not a negative delay.
    val now = Instant.parse("2026-09-07T16:30:00Z")
    assertEquals(22.hours + 30.minutes, untilNext(LocalTime(20, 0), now, karachi))
  }
}

class AlertLogTest {

  @Test
  fun `an unchanged occurrence is only news the first time`() {
    val first = AlertLog(emptyMap())
    assertTrue(first.isNew("bill:rent", "2026-09-01"))

    val second = AlertLog(first.seen)
    assertFalse(second.isNew("bill:rent", "2026-09-01"))
    assertTrue(second.isNew("bill:rent", "2026-10-01"))
  }

  @Test
  fun `subjects not seen this run drop out`() {
    val previous = mapOf("bill:rent" to "2026-09-01", "bill:deleted" to "2026-09-01")
    val log = AlertLog(previous)
    log.isNew("bill:rent", "2026-09-01")
    assertEquals(mapOf("bill:rent" to "2026-09-01"), log.seen)
  }
}
