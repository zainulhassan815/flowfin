package com.flowfin.feature.debts.di

import com.flowfin.feature.debts.DebtsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val debtsModule = module {
  viewModelOf(::DebtsViewModel)
}
