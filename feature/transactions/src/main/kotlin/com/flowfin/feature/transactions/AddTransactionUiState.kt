package com.flowfin.feature.transactions

import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import androidx.annotation.StringRes
import com.flowfin.core.model.Money
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState
import kotlinx.datetime.LocalDate
import org.dreamerslab.formz.Form
import org.dreamerslab.formz.FormInput

/** The three user-facing entry kinds (the scope tabs). */
enum class EntryType { Expense, Income, Transfer }

/** Category scope a type tags against, or null for transfers (no category). */
val EntryType.scope: CategoryScope?
  get() = when (this) {
    EntryType.Expense -> CategoryScope.EXPENSE
    EntryType.Income -> CategoryScope.INCOME
    EntryType.Transfer -> null
  }

/** Which picker sheet is open. */
enum class AddSheet { FromAccount, ToAccount, Category, Date, Note }

/** Picker options (display + the id the form input holds). */
data class AccountOption(val id: AccountId, val name: String, val iconKey: String?, val colorKey: String?, val balance: String, val isBudget: Boolean)
data class CategoryOption(val id: CategoryId, val name: String, val iconKey: String?, val colorKey: String?)

enum class AmountError { Empty }
enum class RequiredError { Missing }

class AmountInput(value: Money?, isPristine: Boolean = true) : FormInput<Money?, AmountError>(value, isPristine) {
  override fun validator(value: Money?): AmountError? = if (value == null || !value.isPositive) AmountError.Empty else null
}

class AccountSelection(value: AccountId?, isPristine: Boolean = true) : FormInput<AccountId?, RequiredError>(value, isPristine) {
  override fun validator(value: AccountId?): RequiredError? = if (value == null) RequiredError.Missing else null
}

class CategorySelection(value: CategoryId?, isPristine: Boolean = true) : FormInput<CategoryId?, RequiredError>(value, isPristine) {
  override fun validator(value: CategoryId?): RequiredError? = if (value == null) RequiredError.Missing else null
}

/**
 * Add-Transaction form state. Implements formz [Form]: the inputs relevant to the
 * current [type] drive [canSave] (Transfer drops the category and adds a second
 * account). Calculator output is mirrored into [amount] (settled) for validity, and
 * into the display strings for the hero.
 */
data class AddTransactionUiState(
  val date: LocalDate,
  val type: EntryType = EntryType.Expense,
  val calculator: CalculatorState = CalculatorState(),
  val amountWhole: String = "0",
  val amountDecimal: String? = null,
  val expression: String? = null,
  val amount: AmountInput = AmountInput(null),
  val fromAccount: AccountSelection = AccountSelection(null),
  val toAccount: AccountSelection = AccountSelection(null),
  val category: CategorySelection = CategorySelection(null),
  val note: String = "",
  val accountOptions: List<AccountOption> = emptyList(),
  val categoryOptions: List<CategoryOption> = emptyList(),
  val openSheet: AddSheet? = null,
  /** A money event, so the form opens on the amount and the keypad opens with it. */
  val amountFocused: Boolean = true,
  val submitting: Boolean = false,
) : Form {
  override val inputs: List<FormInput<*, *>> = when (type) {
    EntryType.Expense -> listOf(amount, fromAccount, category)
    EntryType.Income -> listOf(amount, toAccount, category)
    EntryType.Transfer -> listOf(amount, fromAccount, toAccount)
  }

  val canSave: Boolean get() = isValid && !submitting

  /**
   * The first thing still missing, in reading order — what a blocked Save says
   * instead of sitting inert. Null once the form is valid.
   */
  @get:StringRes
  val blockedReason: Int?
    get() = when {
      !amount.isValid -> R.string.add_tx_blocked_amount
      type != EntryType.Income && !fromAccount.isValid -> R.string.add_tx_blocked_from
      type == EntryType.Income && !toAccount.isValid -> R.string.add_tx_blocked_from_income
      type == EntryType.Transfer && !toAccount.isValid -> R.string.add_tx_blocked_to
      type != EntryType.Transfer && !category.isValid -> R.string.add_tx_blocked_category
      else -> null
    }

  fun selectedFrom(): AccountOption? = accountOptions.firstOrNull { it.id == fromAccount.value }
  fun selectedTo(): AccountOption? = accountOptions.firstOrNull { it.id == toAccount.value }
  fun selectedCategory(): CategoryOption? = categoryOptions.firstOrNull { it.id == category.value }
}
