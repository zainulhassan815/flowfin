package com.flowfin.core.ui

import com.flowfin.core.designsystem.component.CalculatorKey
import com.flowfin.core.designsystem.component.CalculatorOperator
import com.flowfin.core.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

private const val MAX_DIGITS = 12

/**
 * A small money calculator: left-to-right with a single pending operator (no
 * precedence), plus a unary `%`. Pure and deterministic — [press] returns the next
 * state. The recorded amount is [settled] then [value]; mid-expression display is
 * the live [operand].
 */
data class CalculatorState(
  val operand: String = "0",
  val accumulator: BigDecimal? = null,
  val pendingOp: CalculatorOperator? = null,
  val freshOperand: Boolean = true,
) {
  /** Operand as Money (rounded to paise); null only if somehow unparseable. */
  val value: Money?
    get() = operand.toBigDecimalOrNull()
      ?.multiply(HUNDRED)
      ?.setScale(0, RoundingMode.HALF_UP)
      ?.let { Money(it.toLong()) }

  val wholeDigits: String get() = operand.substringBefore(".").ifEmpty { "0" }

  /** The dotted decimal as typed (`.5`, `.50`), or null until a decimal point is entered. */
  val decimalPart: String? get() = if ('.' in operand) "." + operand.substringAfter(".").take(2) else null

  /** The running "accumulator op" line shown above the amount, or null when none is pending. */
  fun expression(format: (BigDecimal) -> String): String? =
    if (accumulator != null && pendingOp != null && pendingOp != CalculatorOperator.Percent) {
      "${format(accumulator)} ${pendingOp.symbol}"
    } else {
      null
    }

  /** Resolve any pending operator so [value] reflects the whole expression (used on save). */
  fun settled(): CalculatorState =
    if (pendingOp != null && pendingOp != CalculatorOperator.Percent) press(CalculatorKey.Equals) else this
}

fun CalculatorState.press(key: CalculatorKey): CalculatorState = when (key) {
  is CalculatorKey.Digit -> digit(key.value)
  CalculatorKey.Decimal -> decimal()
  is CalculatorKey.Operator -> operator(key.type)
  CalculatorKey.Equals -> equals()
  CalculatorKey.Backspace -> backspace()
  CalculatorKey.Clear -> CalculatorState()
}

private fun CalculatorState.digit(d: Int): CalculatorState {
  val next = when {
    freshOperand -> d.toString()
    operand == "0" -> d.toString()
    operand.count { it.isDigit() } >= MAX_DIGITS -> operand
    else -> operand + d
  }
  return copy(operand = next, freshOperand = false)
}

private fun CalculatorState.decimal(): CalculatorState = when {
  freshOperand -> copy(operand = "0.", freshOperand = false)
  '.' in operand -> this
  else -> copy(operand = "$operand.")
}

private fun CalculatorState.operator(op: CalculatorOperator): CalculatorState {
  if (op == CalculatorOperator.Percent) {
    val v = operand.toBigDecimalOrNull() ?: return this
    return copy(operand = v.divide(HUNDRED).stripTrailingZeros().toPlainString(), freshOperand = true)
  }
  val current = operand.toBigDecimalOrNull() ?: return this
  val acc = when {
    accumulator == null -> current
    freshOperand -> accumulator // operator pressed again before a new operand — just swap it
    else -> apply(accumulator, pendingOp, current)
  }
  return copy(accumulator = acc, pendingOp = op, operand = acc.toPlainString(), freshOperand = true)
}

private fun CalculatorState.equals(): CalculatorState {
  val op = pendingOp ?: return this
  val acc = accumulator ?: return this
  val current = operand.toBigDecimalOrNull() ?: return this
  return CalculatorState(operand = apply(acc, op, current).stripTrailingZeros().toPlainString(), freshOperand = true)
}

private fun CalculatorState.backspace(): CalculatorState {
  if (freshOperand) return this
  val next = operand.dropLast(1)
  return copy(operand = next.ifEmpty { "0" }, freshOperand = next.isEmpty())
}

private fun apply(a: BigDecimal, op: CalculatorOperator?, b: BigDecimal): BigDecimal = when (op) {
  CalculatorOperator.Plus -> a + b
  CalculatorOperator.Minus -> a - b
  CalculatorOperator.Times -> a * b
  CalculatorOperator.Divide -> if (b.signum() == 0) a else a.divide(b, 2, RoundingMode.HALF_UP)
  else -> b
}

private val HUNDRED = BigDecimal(100)
