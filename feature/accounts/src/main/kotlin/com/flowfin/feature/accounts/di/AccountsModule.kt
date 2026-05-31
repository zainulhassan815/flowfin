package com.flowfin.feature.accounts.di

import com.flowfin.core.model.AccountId
import com.flowfin.feature.accounts.AccountDetailViewModel
import com.flowfin.feature.accounts.AccountsListViewModel
import com.flowfin.feature.accounts.AddAccountViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val accountsModule = module {
  viewModelOf(::AccountsListViewModel)
  viewModelOf(::AddAccountViewModel)
  // The detail VM takes the opened account's id as a runtime parameter.
  viewModel { (accountId: AccountId) -> AccountDetailViewModel(accountId, get(), get(), get(), get(), get()) }
}
