package com.flowfin.di

import com.flowfin.ui.FlowFinAppViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Shell-scoped dependencies that aren't owned by any one feature. */
val appModule = module {
  viewModelOf(::FlowFinAppViewModel)
}
