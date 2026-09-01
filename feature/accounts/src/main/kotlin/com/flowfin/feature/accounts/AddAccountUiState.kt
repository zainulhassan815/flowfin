package com.flowfin.feature.accounts

import androidx.annotation.StringRes
import com.flowfin.core.model.Money
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState

/**
 * A real account's "kind" — a creation-time preset, not a stored field. It seeds the
 * icon, colour, and name hint; [com.flowfin.core.model.Account] only persists the
 * resulting `icon`/`color`.
 */
enum class AccountKind { Bank, Cash, Mobile }

@get:StringRes
val AccountKind.labelRes: Int
  get() = when (this) {
    AccountKind.Bank -> R.string.account_kind_bank
    AccountKind.Cash -> R.string.account_kind_cash
    AccountKind.Mobile -> R.string.account_kind_mobile
  }

@get:StringRes
val AccountKind.nameHintRes: Int
  get() = when (this) {
    AccountKind.Bank -> R.string.add_account_name_hint_bank
    AccountKind.Cash -> R.string.add_account_name_hint_cash
    AccountKind.Mobile -> R.string.add_account_name_hint_mobile
  }

/** Resolved by `categoryIcon` / `categoryColor` in core:ui. */
val AccountKind.iconKey: String
  get() = when (this) {
    AccountKind.Bank -> "bank"
    AccountKind.Cash -> "wallet"
    AccountKind.Mobile -> "mobile"
  }

val AccountKind.colorKey: String
  get() = when (this) {
    AccountKind.Bank -> "bank"
    AccountKind.Cash -> "cash"
    AccountKind.Mobile -> "mobile"
  }

/** [Blank] only disables Save (no scary error); [Taken] surfaces an inline message. */
enum class NameError { Blank, Taken }

/**
 * The Add-Account form. [name]/[kind]/[balance] are the editable inputs; [nameError]
 * is recomputed live in the ViewModel against existing accounts. [balance] is the raw
 * major-unit text the user types (parsed to [com.flowfin.core.model.Money] on save).
 */
data class AddAccountUiState(
  val currency: String,
  val name: String = "",
  val kind: AccountKind = AccountKind.Bank,
  /** Null until the user picks one — then it overrides [AccountKind.colorKey]. */
  val colorKey: String? = null,
  val balance: String = "",
  val calculator: CalculatorState = CalculatorState(),
  val amountWhole: String = "0",
  val amountDecimal: String? = null,
  val expression: String? = null,
  val openingBalance: Money? = null,
  /** A thing you name, so the form opens on the name with the pad down. */
  val amountFocused: Boolean = false,
  val nameError: NameError? = NameError.Blank,
  val submitting: Boolean = false,
) {
  val canSave: Boolean get() = nameError == null && !submitting

  /** What a blocked Save says instead of sitting inert. An opening balance of zero
   *  is legitimate — a fresh envelope of cash — so it never blocks. */
  @get:StringRes
  val blockedReason: Int?
    get() = when (nameError) {
      NameError.Blank -> R.string.add_account_blocked_name
      NameError.Taken -> R.string.add_account_blocked_taken
      null -> null
    }
}

/**
 * The tints a real account can take. Kind seeds one; this lets the user differ.
 *
 * `edu` is deliberately absent: it is #2C7B87 to `bank`'s #2F7C8E, which is the same
 * teal to any eye, and two swatches you can't tell apart are two cells that do the
 * same thing.
 */
val ACCOUNT_COLOR_KEYS = listOf("bank", "cash", "mobile", "salary", "transport", "shop", "health", "subs")
