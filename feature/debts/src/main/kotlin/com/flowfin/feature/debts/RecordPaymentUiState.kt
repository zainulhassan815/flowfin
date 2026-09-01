package com.flowfin.feature.debts

import androidx.annotation.StringRes
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Money
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.UiText
import kotlinx.datetime.LocalDate

/** Which picker the record-payment form has open, if any. */
enum class RecordPaymentSheet { Date, Note }

/**
 * Record a payment against one debt — a screen, not a sheet.
 *
 * It was a bottom sheet that had to open its own sheets for the date and the note,
 * and whose amount used the system keyboard, which then covered everything below
 * it. A form with four inputs is a form; it gets the same anatomy as the rest —
 * amount hero, one field idiom, and a pinned dock whose keypad follows focus.
 *
 * A money event, so it opens on the amount with the pad up.
 */
data class RecordPaymentUiState(
  val loading: Boolean = true,
  val notFound: Boolean = false,
  val currency: String = "Rs",
  val title: UiText = UiText.Res(R.string.debt_detail_sheet_title),
  val amountLabel: UiText = UiText.Res(R.string.debt_detail_sheet_amount_label),
  val calculator: CalculatorState = CalculatorState(),
  val amountWhole: String = "0",
  val amountDecimal: String? = null,
  val expression: String? = null,
  val amount: Money? = null,
  val amountFocused: Boolean = true,
  val personName: String = "",
  val avatarTintIndex: Int = 1,
  val reason: String? = null,
  val remainingWhole: String = "0",
  val remainingDecimal: String = "",
  val remainingLabel: UiText = UiText.Res(R.string.debts_amount_label_i_owe),
  /** Where the debt lands if this payment is saved — null until an amount is typed. */
  val afterRemaining: UiText? = null,
  /** Off when the repayment is off-book — no account movement is recorded. */
  val linkAccount: Boolean = false,
  val linkLabel: UiText = UiText.Res(R.string.debt_detail_sheet_link_label),
  val linkDescription: UiText = UiText.Res(R.string.debt_detail_sheet_link_desc),
  val accounts: List<RepaymentAccountUi> = emptyList(),
  val selectedAccountId: AccountId? = null,
  val dateLabel: UiText = UiText.Raw(""),
  val date: LocalDate? = null,
  val note: String = "",
  val openSheet: RecordPaymentSheet? = null,
  val saveLabel: UiText = UiText.Res(R.string.debt_detail_sheet_save),
  val saving: Boolean = false,
) {
  /** The first thing still missing, in reading order — what a blocked Save says. */
  @get:StringRes
  val blockedReason: Int?
    get() = when {
      amount?.isPositive != true -> R.string.add_debt_blocked_amount
      linkAccount && selectedAccountId == null -> R.string.add_debt_blocked_account
      else -> null
    }

  val canSave: Boolean get() = blockedReason == null && !saving
}
