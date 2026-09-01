package com.flowfin.feature.accounts

import androidx.annotation.StringRes
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Money
import com.flowfin.core.resources.R
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.UiText

/** Which sheet the Add-Budget form has open, if any. */
enum class AddBudgetSheet { Parent, RefillDay }

/** A real account a budget can be funded from. */
data class BudgetParentOption(
  val id: AccountId,
  val name: String,
  val iconKey: String?,
  val colorKey: String?,
  val balance: String,
)

/**
 * The Add-Budget form.
 *
 * A budget is an envelope: money physically moves into it, and what's left in it is
 * the truth. [refillMonthly] adds the rhythm on top — a funding schedule that tops
 * it up on [refillDay] every month — so the envelope keeps carrying over while the
 * user still gets a monthly figure to read a budget against.
 *
 * Funding is monthly-only by design. Weekly or yearly envelope funding isn't a thing
 * anyone asked for, and [com.flowfin.core.model.Recurrence] can express it whenever
 * they do.
 */
data class AddBudgetUiState(
  val currency: String,
  val name: String = "",
  val iconKey: String = BUDGET_ICON_KEYS.first(),
  val colorKey: String = BUDGET_COLOR_KEYS.first(),
  val parent: AccountId? = null,
  val parentOptions: List<BudgetParentOption> = emptyList(),
  val calculator: CalculatorState = CalculatorState(),
  val amountWhole: String = "0",
  val amountDecimal: String? = null,
  val expression: String? = null,
  val amount: Money? = null,
  val amountFocused: Boolean = false,
  val refillMonthly: Boolean = true,
  val refillDay: Int = 1,
  val nextRefill: UiText? = null,
  val fundNow: Boolean = true,
  val openSheet: AddBudgetSheet? = null,
  val nameError: NameError? = NameError.Blank,
  val submitting: Boolean = false,
) {
  val selectedParent: BudgetParentOption? get() = parentOptions.firstOrNull { it.id == parent }

  /**
   * The one thing still missing, in reading order — the form's own explanation for
   * why Save is off. Null once it can be saved.
   */
  @get:StringRes
  val blockedReason: Int?
    get() = when {
      nameError != null -> R.string.add_budget_blocked_name
      parent == null -> R.string.add_budget_blocked_parent
      amount == null || !amount.isPositive -> R.string.add_budget_blocked_amount
      else -> null
    }

  val canSave: Boolean get() = blockedReason == null && !submitting
}

/**
 * The glyphs a budget can take. Same rule as the category picker: every key must
 * resolve to its own icon in `categoryIcon`, or the grid offers two cells that draw
 * the same thing. Deriving the glyph from the colour was worse — it gave a budget
 * named "Health" the food icon because the colour still sat at its default.
 */
val BUDGET_ICON_KEYS = listOf(
  "restaurant", "shopping_cart", "directions_bus", "bolt", "home", "shopping_bag",
  "movie", "favorite", "school", "spa", "subscriptions", "wallet",
)

/** The colour tokens a budget can take — the set `categoryColor` resolves. */
val BUDGET_COLOR_KEYS = listOf(
  "food", "grocery", "transport", "utilities", "rent",
  "shop", "health", "edu", "care", "subs",
)
