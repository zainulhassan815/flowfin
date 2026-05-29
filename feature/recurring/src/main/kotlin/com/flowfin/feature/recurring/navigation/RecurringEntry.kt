package com.flowfin.feature.recurring.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.RecurringRoute
import com.flowfin.core.ui.ComingSoonScreen

fun EntryProviderScope<NavKey>.recurringEntry() {
  entry<RecurringRoute> { ComingSoonScreen("Recurring") }
}
