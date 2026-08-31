package com.flowfin.core.ui

import com.flowfin.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Grouping is deliberately not asserted here. `en-IN` yields lakh grouping
 * (`1,84,000`) on Android but Western grouping (`184,000`) on the JVM these
 * tests run on — the CLDR data differs — so asserting it would test the
 * toolchain rather than the formatter. What's checked is the behaviour this
 * class actually owns: when a fraction appears, and how compact picks a unit.
 */
class MoneyFormatterTest {

  private val money = MoneyFormatter()

  @Test
  fun `a whole amount carries no fraction`() {
    assertEquals("", money.fraction(Money(184_000_00)))
    assertFalse(money.display(Money(184_000_00)).contains('.'))
  }

  @Test
  fun `paise still show`() {
    assertEquals(".50", money.fraction(Money(1_250_50)))
    assertEquals(".05", money.fraction(Money(1_250_05)))
    assertTrue(money.display(Money(1_250_50)).endsWith(".50"))
  }

  @Test
  fun `zero reads as a plain zero`() {
    assertEquals("Rs 0", money.display(Money.ZERO))
  }

  @Test
  fun `compact switches units at the South-Asian boundaries`() {
    assertEquals("999", money.compact(Money(999_00)))
    assertEquals("45K", money.compact(Money(45_000_00)))
    // 1,00,000 is one lakh, not "100K" — the grouping reads it that way too.
    assertEquals("1L", money.compact(Money(100_000_00)))
    assertEquals("1.5L", money.compact(Money(150_000_00)))
    assertEquals("1Cr", money.compact(Money(10_000_000_00)))
    assertEquals("2.4Cr", money.compact(Money(24_000_000_00)))
  }

  @Test
  fun `compact keeps the sign and drops a trailing zero decimal`() {
    assertEquals("-45K", money.compact(Money(-45_000_00)))
    assertEquals("31.6K", money.compact(Money(31_600_00)))
  }
}
