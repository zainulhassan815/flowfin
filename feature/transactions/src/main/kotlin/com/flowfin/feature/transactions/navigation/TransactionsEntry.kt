package com.flowfin.feature.transactions.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.TransactionsRoute
import com.flowfin.core.ui.ComingSoonScreen

/** The full transactions list — reached from Home's "Recent · All / History". */
fun EntryProviderScope<NavKey>.transactionsEntry() {
  entry<TransactionsRoute> { ComingSoonScreen("Transactions") }
}
