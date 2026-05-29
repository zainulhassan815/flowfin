package com.flowfin.core.ui

import com.flowfin.core.model.Money
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Formats [Money] for display. Grouping is South-Asian (lakh: `1,50,000`) to suit
 * PKR. [whole] carries a leading `-` for negative amounts (e.g. a negative total);
 * transaction rows supply their own semantic +/- sign over the unsigned magnitude.
 *
 * Currency and locale are constructor params so Settings can drive them later.
 */
class MoneyFormatter(
  val symbol: String = "Rs",
  locale: Locale = Locale.forLanguageTag("en-IN"),
) {
  private val grouping: NumberFormat = NumberFormat.getIntegerInstance(locale)

  /** The whole part with grouping, signed if negative: `1,50,000` or `-500`. */
  fun whole(money: Money): String {
    val sign = if (money.minorUnits < 0) "-" else ""
    return sign + grouping.format(money.minorUnits.absoluteValue / 100)
  }

  /** The two-digit fractional part, dotted: `.00`, `.50`. */
  fun fraction(money: Money): String =
    "." + (money.minorUnits.absoluteValue % 100).toString().padStart(2, '0')

  /** Full display string: `Rs 1,50,000.00`. */
  fun display(money: Money): String = "$symbol ${whole(money)}${fraction(money)}"

  /** Group a plain integer (e.g. the live calculator whole part): `1,50,000`. */
  fun group(value: Long): String = grouping.format(value)
}
