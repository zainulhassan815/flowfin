package com.flowfin.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Resolves the string `color` / `icon` keys stored on categories and accounts to
 * concrete theme colors and Material icons. Keeps the design system model-agnostic
 * (it only knows tokens) while features stay free of icon/color wiring. Unknown or
 * null keys fall back to a neutral default.
 */

@Composable
@ReadOnlyComposable
fun categoryColor(key: String?): Color {
  val categories = FlowFinTheme.colors.categories
  return when (key) {
    "food" -> categories.food
    "grocery" -> categories.grocery
    "transport" -> categories.transport
    "salary" -> categories.salary
    "subs" -> categories.subs
    "rent" -> categories.rent
    "utilities" -> categories.utilities
    "shop" -> categories.shop
    "health" -> categories.health
    "edu" -> categories.edu
    "care" -> categories.care
    "bank" -> categories.bank
    "cash" -> categories.cash
    "debt" -> categories.debt
    else -> categories.other
  }
}

fun categoryIcon(key: String?): ImageVector = when (key) {
  "restaurant" -> FlowFinIcons.Food
  "shopping_cart" -> FlowFinIcons.Groceries
  "directions_bus" -> FlowFinIcons.Transport
  "bolt" -> FlowFinIcons.Utilities
  "home" -> FlowFinIcons.Rent
  "shopping_bag" -> FlowFinIcons.Shopping
  "movie" -> FlowFinIcons.Entertainment
  "favorite" -> FlowFinIcons.Healthcare
  "school" -> FlowFinIcons.Education
  "spa" -> FlowFinIcons.PersonalCare
  "subscriptions" -> FlowFinIcons.Subscriptions
  "sync_alt" -> FlowFinIcons.Transfer
  "payments" -> FlowFinIcons.Salary
  "work" -> FlowFinIcons.Salary
  "store" -> FlowFinIcons.Shopping
  "card_giftcard" -> FlowFinIcons.Other
  "undo" -> FlowFinIcons.Transfer
  "bank" -> FlowFinIcons.Bank
  "wallet" -> FlowFinIcons.Wallet
  else -> FlowFinIcons.Other
}
