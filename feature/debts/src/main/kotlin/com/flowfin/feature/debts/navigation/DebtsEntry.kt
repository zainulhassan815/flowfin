package com.flowfin.feature.debts.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.DebtsRoute
import com.flowfin.core.ui.ComingSoonScreen

fun EntryProviderScope<NavKey>.debtsEntry() {
  entry<DebtsRoute> { ComingSoonScreen("Debts") }
}
