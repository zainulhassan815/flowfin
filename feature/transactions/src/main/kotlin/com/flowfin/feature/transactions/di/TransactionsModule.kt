package com.flowfin.feature.transactions.di

import com.flowfin.feature.transactions.AddTransactionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val transactionsModule = module {
  viewModelOf(::AddTransactionViewModel)
}
