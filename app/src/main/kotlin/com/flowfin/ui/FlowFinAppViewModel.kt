package com.flowfin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Shell state for [FlowFinApp]. The add-transaction FAB needs somewhere to post
 * to, so it only appears once at least one account exists — an app-wide rule, not
 * a per-screen one, which is why it lives with the shell rather than inside Home.
 */
class FlowFinAppViewModel(accounts: AccountRepository) : ViewModel() {
  val canAddTransaction: StateFlow<Boolean> =
    accounts.observeActiveAccounts()
      .map { it.isNotEmpty() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)
}

private const val STOP_TIMEOUT_MS = 5_000L
