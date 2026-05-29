package com.flowfin.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.flowfin.core.designsystem.component.FlowFinFab
import com.flowfin.core.designsystem.component.FlowFinNavBar
import com.flowfin.core.designsystem.component.FlowFinNavBarItem
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.navigation.AccountsRoute
import com.flowfin.core.navigation.AddTransactionRoute
import com.flowfin.core.navigation.DebtsRoute
import com.flowfin.core.navigation.HomeRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.core.navigation.RecurringRoute
import com.flowfin.core.navigation.ReportsRoute
import com.flowfin.core.navigation.TOP_LEVEL_ROUTES
import com.flowfin.core.navigation.rememberNavigationState
import com.flowfin.core.navigation.toEntries
import com.flowfin.feature.accounts.navigation.accountsEntry
import com.flowfin.feature.debts.navigation.debtsEntry
import com.flowfin.feature.home.navigation.homeEntry
import com.flowfin.feature.recurring.navigation.recurringEntry
import com.flowfin.feature.reports.navigation.reportsEntry
import com.flowfin.feature.transactions.navigation.addTransactionEntry

private data class Tab(val route: NavKey, val icon: ImageVector, val label: String)

private val TABS = listOf(
  Tab(HomeRoute, Icons.Rounded.Home, "Home"),
  Tab(AccountsRoute, Icons.Rounded.AccountBalanceWallet, "Accounts"),
  Tab(RecurringRoute, Icons.Rounded.Autorenew, "Recur"),
  Tab(DebtsRoute, Icons.Rounded.People, "Debts"),
  Tab(ReportsRoute, Icons.Rounded.BarChart, "Reports"),
)

/** The app shell: one Scaffold owning the bottom nav + FAB, hosting the nav back stack. */
@Composable
fun FlowFinApp() {
  val navState = rememberNavigationState(HomeRoute, TOP_LEVEL_ROUTES.toSet())
  val navigator = remember(navState) { Navigator(navState) }

  val entryProvider = entryProvider {
    homeEntry()
    accountsEntry()
    recurringEntry()
    debtsEntry()
    reportsEntry()
    addTransactionEntry()
  }

  // Nav bar + FAB only on a tab root, not on pushed screens (e.g. Add Transaction).
  val onTabRoot = navState.currentKey == navState.currentTopLevelKey

  Scaffold(
    containerColor = FlowFinTheme.colors.bg,
    bottomBar = {
      if (onTabRoot) {
        FlowFinNavBar {
          TABS.forEach { tab ->
            FlowFinNavBarItem(
              icon = tab.icon,
              label = tab.label,
              selected = navState.currentTopLevelKey == tab.route,
              onClick = { navigator.navigate(tab.route) },
            )
          }
        }
      }
    },
    floatingActionButton = {
      if (onTabRoot) {
        FlowFinFab(
          onClick = { navigator.navigate(AddTransactionRoute) },
          icon = Icons.Rounded.Add,
          contentDescription = "Add transaction",
        )
      }
    },
  ) { innerPadding ->
    NavDisplay(
      entries = navState.toEntries(entryProvider),
      onBack = { navigator.goBack() },
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    )
  }
}
