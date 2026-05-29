package com.flowfin.core.data

import com.flowfin.core.model.CategoryScope

/**
 * A shipped default category: [color] is a `FlowFinColors.categories` token key
 * and [icon] a Material icon key, both resolved to real values by the UI layer.
 */
internal data class DefaultCategory(
  val name: String,
  val scope: CategoryScope,
  val icon: String,
  val color: String,
)

/**
 * The default categories seeded at first launch (docs/prd.md §6.2.3 and §6.4.3).
 * Order here is the display order within each scope. First-pass icon/color keys —
 * the Categories design pass can refine them.
 */
internal val DEFAULT_CATEGORIES: List<DefaultCategory> = listOf(
  DefaultCategory("Food & Dining", CategoryScope.EXPENSE, icon = "restaurant", color = "food"),
  DefaultCategory("Groceries", CategoryScope.EXPENSE, icon = "shopping_cart", color = "grocery"),
  DefaultCategory("Transport", CategoryScope.EXPENSE, icon = "directions_bus", color = "transport"),
  DefaultCategory("Utilities", CategoryScope.EXPENSE, icon = "bolt", color = "utilities"),
  DefaultCategory("Rent", CategoryScope.EXPENSE, icon = "home", color = "rent"),
  DefaultCategory("Shopping", CategoryScope.EXPENSE, icon = "shopping_bag", color = "shop"),
  DefaultCategory("Entertainment", CategoryScope.EXPENSE, icon = "movie", color = "subs"),
  DefaultCategory("Healthcare", CategoryScope.EXPENSE, icon = "favorite", color = "health"),
  DefaultCategory("Education", CategoryScope.EXPENSE, icon = "school", color = "edu"),
  DefaultCategory("Personal Care", CategoryScope.EXPENSE, icon = "spa", color = "care"),
  DefaultCategory("Subscriptions", CategoryScope.EXPENSE, icon = "subscriptions", color = "subs"),
  DefaultCategory("Debt Repayment", CategoryScope.EXPENSE, icon = "sync_alt", color = "other"),
  DefaultCategory("Other", CategoryScope.EXPENSE, icon = "category", color = "other"),
  DefaultCategory("Salary", CategoryScope.INCOME, icon = "payments", color = "salary"),
  DefaultCategory("Freelance", CategoryScope.INCOME, icon = "work", color = "salary"),
  DefaultCategory("Business", CategoryScope.INCOME, icon = "store", color = "salary"),
  DefaultCategory("Gift", CategoryScope.INCOME, icon = "card_giftcard", color = "care"),
  DefaultCategory("Refund", CategoryScope.INCOME, icon = "undo", color = "grocery"),
  DefaultCategory("Other", CategoryScope.INCOME, icon = "category", color = "other"),
)
