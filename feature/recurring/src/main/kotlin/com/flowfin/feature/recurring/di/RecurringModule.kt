package com.flowfin.feature.recurring.di

import com.flowfin.feature.recurring.RecurringViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recurringModule = module {
  viewModelOf(::RecurringViewModel)
}
