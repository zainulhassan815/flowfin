package com.flowfin.feature.accounts

import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText

/** One-shot effects from the ViewModel, consumed in the entry's LaunchedEffect. */
sealed interface AddAccountEffect {
  data object NavigateBack : AddAccountEffect
  data class ShowMessage(val text: UiText) : AddAccountEffect
}

/**
 * Maps a save failure to a snackbar message. [AccountError.DuplicateName] is normally
 * caught live and shown inline under the name; this covers the rare save-race and the
 * [AccountError.Unexpected] backstop. The budget-only and blank cases can't occur here
 * (real account; Save is gated on a non-blank name).
 */
internal fun AccountError.toMessage(): UiText = when (this) {
  is AccountError.DuplicateName -> UiText.Res(R.string.add_account_duplicate_name, listOf(name))
  else -> UiText.Res(R.string.add_account_error_generic)
}
