package com.flowfin.feature.accounts

import androidx.annotation.StringRes
import com.flowfin.core.resources.R

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
  val balance: String = "",
  val nameError: NameError? = NameError.Blank,
  val submitting: Boolean = false,
) {
  val canSave: Boolean get() = nameError == null && !submitting
}
