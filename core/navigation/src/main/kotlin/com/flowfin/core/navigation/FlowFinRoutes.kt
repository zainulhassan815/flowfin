package com.flowfin.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Every destination, as a typed [NavKey]. The five tab destinations are the
 * top-level keys ([TOP_LEVEL_ROUTES]); the rest are pushed within a tab's stack.
 * Routes that need arguments later get typed constructor fields.
 */

@Serializable data object HomeRoute : NavKey

@Serializable data object AccountsRoute : NavKey

@Serializable data object RecurringRoute : NavKey

@Serializable data object DebtsRoute : NavKey

@Serializable data object ReportsRoute : NavKey

@Serializable data object AddTransactionRoute : NavKey

@Serializable data object TransactionsRoute : NavKey

@Serializable data object SettingsRoute : NavKey

/** The bottom-nav tabs, in order. [HomeRoute] is the start destination. */
val TOP_LEVEL_ROUTES: List<NavKey> = listOf(
  HomeRoute,
  AccountsRoute,
  RecurringRoute,
  DebtsRoute,
  ReportsRoute,
)
