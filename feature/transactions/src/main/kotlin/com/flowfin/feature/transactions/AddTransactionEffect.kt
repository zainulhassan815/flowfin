package com.flowfin.feature.transactions

import com.flowfin.core.domain.error.TransactionError

/** One-shot effects from the ViewModel, consumed in the entry's LaunchedEffect. */
sealed interface AddTransactionEffect {
  data object NavigateBack : AddTransactionEffect
  data class ShowMessage(val text: String) : AddTransactionEffect
}

/** Maps a typed save failure to a user-facing message. */
internal fun TransactionError.toMessage(): String = when (this) {
  TransactionError.AmountNotPositive -> "Enter an amount greater than zero."
  is TransactionError.AccountNotFound -> "That account no longer exists."
  is TransactionError.ArchivedAccount -> "That account is archived."
  is TransactionError.InvalidAccountForKind -> reason.replaceFirstChar { it.uppercase() }
  TransactionError.SelfTransfer -> "Pick two different accounts."
  TransactionError.CurrencyMismatch -> "Those accounts use different currencies."
  is TransactionError.CategoryNotFound -> "That category no longer exists."
  TransactionError.CategoryScopeMismatch -> "Pick a category for this type."
  is TransactionError.Unexpected -> "Couldn't save. Please try again."
}
