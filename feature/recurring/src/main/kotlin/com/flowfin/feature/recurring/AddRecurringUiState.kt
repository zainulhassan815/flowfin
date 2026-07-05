package com.flowfin.feature.recurring

import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.Recurrence
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.UiText

/** What a schedule fires — the two kinds the database allows a `recurringId` on. */
enum class RecurringEntryType { Expense, Income }

val RecurringEntryType.scope: CategoryScope
  get() = when (this) {
    RecurringEntryType.Expense -> CategoryScope.EXPENSE
    RecurringEntryType.Income -> CategoryScope.INCOME
  }

/** Which picker sheet is open. */
enum class AddRecurringSheet { Amount, RepeatsOn, Account, Category, Note }

/** Picker options (display + the id the form holds). */
data class RecurringAccountOption(
  val id: AccountId,
  val name: String,
  val iconKey: String?,
  val colorKey: String?,
  val balance: String,
  val isBudget: Boolean,
)

data class RecurringCategoryOption(val id: CategoryId, val name: String, val iconKey: String?, val colorKey: String?)

/**
 * The Add-Recurring form. The cadence keeps a remembered day per frequency
 * ([weeklyDay] / [monthlyDay] / [yearlyMonth]+[yearlyDay], seeded from today) so
 * switching the segmented control back and forth doesn't lose a picked day;
 * [recurrence] is the one the current [frequency] points at. [account] is the
 * expense source or the income destination depending on [type].
 *
 * [repeatsOn] and [nextDue] are display projections of the cadence, recomputed
 * by the ViewModel wherever the form changes.
 */
data class AddRecurringUiState(
  val weeklyDay: Int,
  val monthlyDay: Int,
  val yearlyMonth: Int,
  val yearlyDay: Int,
  val type: RecurringEntryType = RecurringEntryType.Expense,
  val calculator: CalculatorState = CalculatorState(),
  val amountWhole: String = "0",
  val amountDecimal: String? = null,
  val expression: String? = null,
  val amount: Money? = null,
  val name: String = "",
  val frequency: RecurringFrequency = RecurringFrequency.Monthly,
  val repeatsOn: UiText = UiText.Raw(""),
  val nextDue: UiText = UiText.Raw(""),
  val account: AccountId? = null,
  val category: CategoryId? = null,
  val note: String = "",
  val accountOptions: List<RecurringAccountOption> = emptyList(),
  val categoryOptions: List<RecurringCategoryOption> = emptyList(),
  val openSheet: AddRecurringSheet? = null,
  val submitting: Boolean = false,
) {
  val recurrence: Recurrence
    get() = when (frequency) {
      RecurringFrequency.Weekly -> Recurrence.Weekly(weeklyDay)
      RecurringFrequency.Monthly -> Recurrence.Monthly(monthlyDay)
      RecurringFrequency.Yearly -> Recurrence.Yearly(yearlyMonth, yearlyDay)
    }

  val canSave: Boolean
    get() = amount?.isPositive == true && name.isNotBlank() && account != null && category != null && !submitting

  fun selectedAccount(): RecurringAccountOption? = accountOptions.firstOrNull { it.id == account }
  fun selectedCategory(): RecurringCategoryOption? = categoryOptions.firstOrNull { it.id == category }
}

enum class RecurringFrequency { Weekly, Monthly, Yearly }
