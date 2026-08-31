package com.flowfin.feature.reports.di

import com.flowfin.feature.reports.ReportsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reportsModule = module {
  viewModelOf(::ReportsViewModel)
}
