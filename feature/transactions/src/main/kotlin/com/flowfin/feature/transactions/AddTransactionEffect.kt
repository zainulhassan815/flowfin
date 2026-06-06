package com.flowfin.feature.transactions

import com.flowfin.core.domain.error.TransactionError
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText

/** One-shot effects from the ViewModel, consumed in the entry's LaunchedEffect. */
sealed interface AddTransactionEffect {
  data object NavigateBack : AddTransactionEffect
  data class ShowMessage(val text: UiText) : AddTransactionEffect
}

/** Maps a typed save failure to a user-facing message. */
internal fun TransactionError.toMessage(): UiText = when (this) {
  TransactionError.AmountNotPositive -> UiText.Res(R.string.add_tx_error_amount)
  is TransactionError.AccountNotFound -> UiText.Res(R.string.add_tx_error_account_not_found)
  is TransactionError.ArchivedAccount -> UiText.Res(R.string.add_tx_error_account_archived)
  // Domain-supplied reason; localizing it is a follow-up (see FLO-25). Surface as-is for now.
  is TransactionError.InvalidAccountForKind -> UiText.Raw(reason.replaceFirstChar { it.uppercase() })
  TransactionError.SelfTransfer -> UiText.Res(R.string.add_tx_error_self_transfer)
  TransactionError.CurrencyMismatch -> UiText.Res(R.string.add_tx_error_currency_mismatch)
  is TransactionError.CategoryNotFound -> UiText.Res(R.string.add_tx_error_category_not_found)
  TransactionError.CategoryScopeMismatch -> UiText.Res(R.string.add_tx_error_category_scope)
  is TransactionError.Unexpected -> UiText.Res(R.string.add_tx_error_generic)
}
