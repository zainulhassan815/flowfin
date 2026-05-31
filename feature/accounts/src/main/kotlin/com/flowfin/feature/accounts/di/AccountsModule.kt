package com.flowfin.feature.accounts.di

import com.flowfin.feature.accounts.AddAccountViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val accountsModule = module {
  viewModelOf(::AddAccountViewModel)
}
