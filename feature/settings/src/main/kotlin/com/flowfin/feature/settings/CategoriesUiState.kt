package com.flowfin.feature.settings

import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.ui.UiText

/** Which editor sheet is open, if any. */
sealed interface CategorySheet {
  /** Creating a custom category in the current scope. */
  data object Add : CategorySheet

  /** Editing one custom category. Defaults are immutable and never open this. */
  data class Edit(val id: CategoryId) : CategorySheet
}

/**
 * Categories, split by scope. Defaults are shipped and immutable — they show
 * their usage but can't be renamed, retinted or archived — so only customs
 * carry a chevron into the editor.
 */
data class CategoriesUiState(
  val scope: CategoryScope = CategoryScope.EXPENSE,
  val defaults: List<CategoryRowUi> = emptyList(),
  val customs: List<CategoryRowUi> = emptyList(),
  val archived: List<CategoryRowUi> = emptyList(),
  val showArchived: Boolean = false,
  val sheet: CategorySheet? = null,
  val form: CategoryFormUi = CategoryFormUi(),
) {
  val summary: CategorySummaryUi
    get() = CategorySummaryUi(defaults.size, customs.size, defaults.sumOf { it.count } + customs.sumOf { it.count })
}

data class CategorySummaryUi(val defaultCount: Int, val customCount: Int, val transactionCount: Int)

data class CategoryRowUi(
  val id: CategoryId,
  val name: String,
  val iconKey: String?,
  val colorKey: String?,
  /** Transactions filed under this category, ever. */
  val count: Int,
  /** Null for defaults, which show a lock instead of provenance. */
  val meta: UiText?,
  val isDefault: Boolean,
)

/** The icon keys a custom category can take — the set `categoryIcon` resolves. */
val CATEGORY_ICON_KEYS = listOf(
  "restaurant", "shopping_cart", "directions_bus", "bolt", "home", "shopping_bag",
  "movie", "favorite", "school", "spa", "subscriptions", "payments", "work",
  "store", "card_giftcard", "bank", "wallet", "mobile",
)

/** The colour keys a custom category can take — the set `categoryColor` resolves. */
val CATEGORY_COLOR_KEYS = listOf(
  "food", "grocery", "transport", "salary", "subs", "rent",
  "utilities", "shop", "health", "edu", "care", "bank", "cash", "mobile",
)

const val DEFAULT_ICON_KEY = "card_giftcard"

/**
 * Must be one of [CATEGORY_COLOR_KEYS] — a default outside the list leaves the
 * colour grid with nothing selected, and "other" resolves to a grey that reads
 * as no choice at all.
 */
val DEFAULT_COLOR_KEY = CATEGORY_COLOR_KEYS.first()

/** The add / edit form. Icon and colour are the shipped keys, not free values. */
data class CategoryFormUi(
  val name: String = "",
  val iconKey: String = DEFAULT_ICON_KEY,
  val colorKey: String = DEFAULT_COLOR_KEY,
  val saving: Boolean = false,
) {
  val canSave: Boolean get() = name.isNotBlank() && !saving
}
