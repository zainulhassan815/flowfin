package com.flowfin.core.model

import kotlin.jvm.JvmInline

/**
 * An amount of money in minor units (paise for PKR). No floats, ever.
 *
 * Currency is a tag on the account, not on the amount — at launch everything
 * is PKR. This type only carries the integer count and the arithmetic over it;
 * presentation formatting (grouping, currency symbol) is a UI concern and lives
 * elsewhere.
 */
@JvmInline
value class Money(val minorUnits: Long) : Comparable<Money> {

  operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

  operator fun minus(other: Money): Money = Money(minorUnits - other.minorUnits)

  operator fun times(factor: Int): Money = Money(minorUnits * factor)

  operator fun unaryMinus(): Money = Money(-minorUnits)

  override fun compareTo(other: Money): Int = minorUnits.compareTo(other.minorUnits)

  val isZero: Boolean get() = minorUnits == 0L

  val isPositive: Boolean get() = minorUnits > 0L

  val isNegative: Boolean get() = minorUnits < 0L

  companion object {
    val ZERO: Money = Money(0L)
  }
}
