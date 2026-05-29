package com.flowfin.feature.transactions.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AddTransactionRoute
import com.flowfin.core.ui.ComingSoonScreen

fun EntryProviderScope<NavKey>.addTransactionEntry() {
  entry<AddTransactionRoute> { ComingSoonScreen("Add Transaction") }
}
