package com.flowfin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.annotation.StringRes
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.flowfin.core.resources.R
import com.flowfin.feature.accounts.navigation.accountDetailEntry
import com.flowfin.feature.accounts.navigation.accountsEntry
import com.flowfin.feature.accounts.navigation.addAccountEntry
import com.flowfin.feature.debts.navigation.debtsEntry
import com.flowfin.feature.home.navigation.homeEntry
import com.flowfin.feature.recurring.navigation.addRecurringEntry
import com.flowfin.feature.recurring.navigation.recurringDetailEntry
import com.flowfin.feature.recurring.navigation.recurringEntry
import com.flowfin.feature.reports.navigation.reportsEntry
import com.flowfin.feature.transactions.navigation.addTransactionEntry
import com.flowfin.feature.transactions.navigation.transactionDetailEntry
import com.flowfin.feature.transactions.navigation.transactionsEntry
import org.koin.compose.viewmodel.koinViewModel

private data class Tab(val route: NavKey, val icon: ImageVector, @StringRes val label: Int)

private val TABS = listOf(
  Tab(HomeRoute, FlowFinIcons.Home, R.string.nav_home),
  Tab(AccountsRoute, FlowFinIcons.Accounts, R.string.nav_accounts),
  Tab(RecurringRoute, FlowFinIcons.Recurring, R.string.nav_recurring),
  Tab(DebtsRoute, FlowFinIcons.Debts, R.string.nav_debts),
  Tab(ReportsRoute, FlowFinIcons.Reports, R.string.nav_reports),
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
    accountsEntry(navigator)
    accountDetailEntry(navigator)
    recurringEntry(navigator)
    recurringDetailEntry(navigator)
    debtsEntry()
    reportsEntry()
    addTransactionEntry(navigator)
    addAccountEntry(navigator)
    addRecurringEntry(navigator)
    transactionsEntry(navigator)
    transactionDetailEntry(navigator)
  }

  // Nav bar + FAB only on a tab root, not on pushed screens (e.g. Add Transaction).
  val onTabRoot = navState.currentKey == navState.currentTopLevelKey

  Scaffold(
    // The shell owns no vertical insets — it draws edge-to-edge and each screen
    // (and the chrome below) clears the status / nav bars itself. Only display
    // cutouts on the sides are consumed here, for the whole app at once.
    containerColor = Color.Transparent,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    bottomBar = {
      if (onTabRoot) {
        FlowFinNavBar {
          TABS.forEach { tab ->
            FlowFinNavBarItem(
              icon = tab.icon,
              label = stringResource(tab.label),
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
          contentDescription = stringResource(R.string.nav_add_transaction),
        )
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .consumeWindowInsets(innerPadding)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      NavDisplay(
        entries = navState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}
