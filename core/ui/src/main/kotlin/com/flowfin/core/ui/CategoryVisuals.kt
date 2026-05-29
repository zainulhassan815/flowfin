package com.flowfin.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material.icons.rounded.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
  "restaurant" -> Icons.Rounded.Restaurant
  "shopping_cart" -> Icons.Rounded.ShoppingCart
  "directions_bus" -> Icons.Rounded.DirectionsBus
  "bolt" -> Icons.Rounded.Bolt
  "home" -> Icons.Rounded.Home
  "shopping_bag" -> Icons.Rounded.ShoppingBag
  "movie" -> Icons.Rounded.Movie
  "favorite" -> Icons.Rounded.Favorite
  "school" -> Icons.Rounded.School
  "spa" -> Icons.Rounded.Spa
  "subscriptions" -> Icons.Rounded.Subscriptions
  "sync_alt" -> Icons.Rounded.SyncAlt
  "payments" -> Icons.Rounded.Payments
  "work" -> Icons.Rounded.Work
  "store" -> Icons.Rounded.Store
  "card_giftcard" -> Icons.Rounded.CardGiftcard
  "undo" -> Icons.AutoMirrored.Rounded.Undo
  "bank" -> Icons.Rounded.AccountBalance
  "wallet" -> Icons.Rounded.AccountBalanceWallet
  else -> Icons.Rounded.Category
}
