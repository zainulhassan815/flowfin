package com.flowfin.feature.accounts.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.navigation.AccountsRoute
import com.flowfin.core.ui.ComingSoonScreen

fun EntryProviderScope<NavKey>.accountsEntry() {
  entry<AccountsRoute> { ComingSoonScreen("Accounts") }
}
