package com.flowfin.feature.transactions.di

import com.flowfin.feature.transactions.AddTransactionViewModel
import com.flowfin.feature.transactions.TransactionsListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val transactionsModule = module {
  viewModelOf(::AddTransactionViewModel)
  viewModelOf(::TransactionsListViewModel)
}
