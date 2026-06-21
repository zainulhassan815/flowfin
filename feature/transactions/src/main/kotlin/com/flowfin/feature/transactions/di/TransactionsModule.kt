package com.flowfin.feature.transactions.di

import com.flowfin.core.model.TransactionId
import com.flowfin.feature.transactions.AddTransactionViewModel
import com.flowfin.feature.transactions.TransactionDetailViewModel
import com.flowfin.feature.transactions.TransactionsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val transactionsModule = module {
  viewModelOf(::AddTransactionViewModel)
  viewModelOf(::TransactionsListViewModel)
  // The detail VM takes the opened transaction's id as a runtime parameter.
  viewModel { (id: TransactionId) -> TransactionDetailViewModel(id, get(), get(), get(), get()) }
}
