package com.flowfin.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Money's operators and sign are hand-written over a Long, so a typo (a minus
 * that adds, an isPositive that allows zero) would compile silently. `isPositive`
 * in particular gates `RecordTransaction`'s amount check.
 */
class MoneyTest {

  @Test
  fun `arithmetic operators combine the minor units`() {
    assertEquals(Money(300), Money(100) + Money(200))
    assertEquals(Money(100), Money(300) - Money(200))
    assertEquals(Money(600), Money(200) * 3)
    assertEquals(Money(-50), -Money(50))
  }

  @Test
  fun `comparison orders amounts by minor units`() {
    assertTrue(Money(100) > Money(50))
    assertTrue(Money(-1) < Money.ZERO)
    assertEquals(0, Money(42).compareTo(Money(42)))
  }

  @Test
  fun `sign distinguishes positive, zero and negative`() {
    assertTrue(Money(1).isPositive)
    assertFalse(Money.ZERO.isPositive)
    assertTrue(Money.ZERO.isZero)
    assertTrue(Money(-1).isNegative)
  }
}
