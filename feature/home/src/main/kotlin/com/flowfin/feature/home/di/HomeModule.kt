package com.flowfin.feature.home.di

import com.flowfin.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
  viewModelOf(::HomeViewModel)
}
