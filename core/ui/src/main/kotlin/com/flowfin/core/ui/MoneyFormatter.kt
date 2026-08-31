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

  /**
   * The dotted fractional part — `.50` — or empty when the amount is whole.
   * Round figures are the common case, and a trailing `.00` on every one of
   * them is three characters of noise carrying nothing.
   */
  fun fraction(money: Money): String {
    val paise = money.minorUnits.absoluteValue % 100
    return if (paise == 0L) "" else "." + paise.toString().padStart(2, '0')
  }

  /** Full display string: `Rs 1,50,000.00`. */
  fun display(money: Money): String = "$symbol ${whole(money)}${fraction(money)}"

  /** Like [display] but without the fraction: `Rs 1,50,000`. For summary totals. */
  fun displayWhole(money: Money): String = "$symbol ${whole(money)}"

  /** Group a plain integer (e.g. the live calculator whole part): `1,50,000`. */
  fun group(value: Long): String = grouping.format(value)

  /**
   * A short form for places with no room — chart ticks, a donut's hole, dense
   * meta — where the exact figure is available nearby.
   *
   * Thousands take `K`, but lakhs and crores take `L` and `Cr` rather than
   * `M`/`B`. Below a lakh both conventions group identically, so `45K` is
   * unambiguous; above it they diverge — this formatter groups South-Asian
   * (`1,50,000` reads as one and a half lakh), and calling that `150K` would
   * assert a different reading of the same digits.
   *
   * Never use this for a headline balance: knowing what is left exactly is the
   * point of the app, and `1.8L` is not that.
   */
  fun compact(money: Money): String {
    val sign = if (money.minorUnits < 0) "-" else ""
    val units = money.minorUnits.absoluteValue / 100
    return sign + when {
      units < 1_000L -> grouping.format(units)
      units < 100_000L -> trim(units / 1_000.0) + "K"
      units < 10_000_000L -> trim(units / 100_000.0) + "L"
      else -> trim(units / 10_000_000.0) + "Cr"
    }
  }

  /** One decimal place, and not even that when it would be `.0`. */
  private fun trim(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10
    return if (rounded == kotlin.math.floor(rounded)) rounded.toLong().toString() else rounded.toString()
  }
}
