package com.flowfin.feature.transactions

import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.CalculatorKey.Backspace
import com.flowfin.core.designsystem.component.CalculatorKey.Clear
import com.flowfin.core.designsystem.component.CalculatorKey.Decimal
import com.flowfin.core.designsystem.component.CalculatorKey.Digit
import com.flowfin.core.designsystem.component.CalculatorKey.Equals
import com.flowfin.core.designsystem.component.CalculatorKey.Operator
import com.flowfin.core.designsystem.component.CalculatorOperator
import com.flowfin.core.model.Money
import org.junit.Test
import kotlin.test.assertEquals

class CalculatorTest {

  private fun run(vararg keys: CalculatorKey) = keys.fold(CalculatorState()) { state, key -> state.press(key) }
  private fun op(o: CalculatorOperator) = Operator(o)

  @Test
  fun `typing digits builds the amount in paise`() {
    assertEquals(Money(12_300), run(Digit(1), Digit(2), Digit(3)).value)
  }

  @Test
  fun `a decimal point captures up to two fractional digits`() {
    assertEquals(Money(1_250), run(Digit(1), Digit(2), Decimal, Digit(5), Digit(0)).value)
  }

  @Test
  fun `addition settles on equals`() {
    assertEquals(Money(1_500), run(Digit(1), Digit(0), op(CalculatorOperator.Plus), Digit(5), Equals).value)
  }

  @Test
  fun `multiplication and division compute, division rounds to paise`() {
    assertEquals(Money(6_000), run(Digit(2), Digit(0), op(CalculatorOperator.Times), Digit(3), Equals).value)
    assertEquals(Money(250), run(Digit(1), Digit(0), op(CalculatorOperator.Divide), Digit(4), Equals).value)
  }

  @Test
  fun `percent divides the current operand by a hundred`() {
    assertEquals(Money(50), run(Digit(5), Digit(0), op(CalculatorOperator.Percent)).value)
  }

  @Test
  fun `division by zero is guarded, not a crash`() {
    assertEquals(Money(500), run(Digit(5), op(CalculatorOperator.Divide), Digit(0), Equals).value)
  }

  @Test
  fun `backspace and clear edit the operand`() {
    assertEquals(Money(1_200), run(Digit(1), Digit(2), Digit(3), Backspace).value)
    assertEquals(Money(0), run(Digit(1), Digit(2), Clear).value)
  }

  @Test
  fun `settled resolves a pending operator without an explicit equals`() {
    val pending = run(Digit(1), Digit(0), op(CalculatorOperator.Plus), Digit(5))
    assertEquals(Money(1_500), pending.settled().value)
  }
}
