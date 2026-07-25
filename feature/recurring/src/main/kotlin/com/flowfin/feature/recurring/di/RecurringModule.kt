package com.flowfin.feature.recurring.di

import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.feature.recurring.AddRecurringViewModel
import com.flowfin.feature.recurring.RecurringDetailViewModel
import com.flowfin.feature.recurring.RecurringViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recurringModule = module {
  viewModelOf(::RecurringViewModel)
  viewModelOf(::AddRecurringViewModel)
  // The detail VM takes the opened schedule's id as a runtime parameter.
  viewModel { (id: RecurringScheduleId) -> RecurringDetailViewModel(id, get(), get(), get(), get()) }
}
