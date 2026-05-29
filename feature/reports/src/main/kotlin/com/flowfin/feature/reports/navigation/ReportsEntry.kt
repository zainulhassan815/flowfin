package com.flowfin.feature.reports.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.ReportsRoute
import com.flowfin.core.ui.ComingSoonScreen

fun EntryProviderScope<NavKey>.reportsEntry() {
  entry<ReportsRoute> { ComingSoonScreen("Reports") }
}
