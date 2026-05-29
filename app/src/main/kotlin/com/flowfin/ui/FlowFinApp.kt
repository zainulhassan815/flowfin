package com.flowfin.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.flowfin.core.designsystem.component.FlowFinFab
import com.flowfin.core.designsystem.component.FlowFinNavBar
import com.flowfin.core.designsystem.component.FlowFinNavBarItem
import com.flowfin.core.designsystem.icon.FlowFinIcons
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
import com.flowfin.feature.transactions.navigation.transactionsEntry
import org.koin.compose.viewmodel.koinViewModel

private data class Tab(val route: NavKey, val icon: ImageVector, val label: String)

private val TABS = listOf(
  Tab(HomeRoute, FlowFinIcons.Home, "Home"),
  Tab(AccountsRoute, FlowFinIcons.Accounts, "Accounts"),
  Tab(RecurringRoute, FlowFinIcons.Recurring, "Recur"),
  Tab(DebtsRoute, FlowFinIcons.Debts, "Debts"),
  Tab(ReportsRoute, FlowFinIcons.Reports, "Reports"),
)

/** The app shell: one Scaffold owning the bottom nav + FAB, hosting the nav back stack. */
@Composable
fun FlowFinApp() {
  val navState = rememberNavigationState(HomeRoute, TOP_LEVEL_ROUTES.toSet())
  val navigator = remember(navState) { Navigator(navState) }

  val appViewModel = koinViewModel<FlowFinAppViewModel>()
  val canAddTransaction by appViewModel.canAddTransaction.collectAsStateWithLifecycle()

  val entryProvider = entryProvider {
    homeEntry(navigator)
    accountsEntry()
    recurringEntry()
    debtsEntry()
    reportsEntry()
    addTransactionEntry(navigator)
    transactionsEntry()
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
      // Adding a transaction needs an account to post to, so the FAB waits for one.
      if (onTabRoot && canAddTransaction) {
        FlowFinFab(
          onClick = { navigator.navigate(AddTransactionRoute) },
          icon = FlowFinIcons.Add,
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
