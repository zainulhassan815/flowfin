package com.flowfin.feature.debts.di

import com.flowfin.core.model.DebtId
import com.flowfin.feature.debts.AddDebtViewModel
import com.flowfin.feature.debts.DebtDetailViewModel
import com.flowfin.feature.debts.DebtsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val debtsModule = module {
  viewModelOf(::DebtsViewModel)
  viewModelOf(::AddDebtViewModel)
  // The detail VM takes the opened debt's id as a runtime parameter.
  viewModel { (id: DebtId) -> DebtDetailViewModel(id, get(), get(), get(), get(), get(), get()) }
}
