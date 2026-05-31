package com.flowfin.feature.accounts.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.flowfin.core.model.AccountId
import com.flowfin.core.navigation.AccountDetailRoute
import com.flowfin.core.navigation.AddTransactionRoute
import com.flowfin.core.navigation.Navigator
import com.flowfin.feature.accounts.AccountDetailScreen
import com.flowfin.feature.accounts.AccountDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

fun EntryProviderScope<NavKey>.accountDetailEntry(navigator: Navigator) {
  entry<AccountDetailRoute> { route ->
    val accountId = AccountId(Uuid.parse(route.accountId))
    val viewModel = koinViewModel<AccountDetailViewModel> { parametersOf(accountId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AccountDetailScreen(
      state = state,
      onBack = { navigator.goBack() },
      // Quick actions open the generic Add-Transaction sheet; pre-selecting this
      // account (and the action's kind) is a follow-up once the route takes args.
      onAddTransaction = { navigator.navigate(AddTransactionRoute) },
    )
  }
}
