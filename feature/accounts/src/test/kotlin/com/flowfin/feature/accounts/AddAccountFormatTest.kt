package com.flowfin.feature.accounts

import com.flowfin.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class AddAccountFormatTest {

  @Test
  fun `sanitizeAmount keeps digits and one capped decimal`() {
    assertEquals("40000", sanitizeAmount("Rs 40,000"))
    assertEquals("12.50", sanitizeAmount("12.50"))
    assertEquals("12.50", sanitizeAmount("12.5099"))   // two fractional places max
    assertEquals("12.5", sanitizeAmount("12.5.9"))      // a stray second dot ends the number
    assertEquals("", sanitizeAmount("abc"))
  }

  @Test
  fun `parseAmount converts major units to minor`() {
    assertEquals(Money(4_000_000), parseAmount("40000"))
    assertEquals(Money(1_250), parseAmount("12.50"))
    assertEquals(Money(1_200), parseAmount("12"))
  }

  @Test
  fun `parseAmount treats blank or partial input as zero`() {
    assertEquals(Money.ZERO, parseAmount(""))
    assertEquals(Money.ZERO, parseAmount("."))
  }

  @Test
  fun `kind maps to the stored icon and colour keys`() {
    assertEquals("bank" to "bank", AccountKind.Bank.iconKey to AccountKind.Bank.colorKey)
    assertEquals("wallet" to "cash", AccountKind.Cash.iconKey to AccountKind.Cash.colorKey)
    assertEquals("mobile" to "mobile", AccountKind.Mobile.iconKey to AccountKind.Mobile.colorKey)
  }
}
