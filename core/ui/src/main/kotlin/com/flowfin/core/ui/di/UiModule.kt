package com.flowfin.core.ui.di

import com.flowfin.core.ui.MoneyFormatter
import org.koin.dsl.module

/** Shared presentation dependencies. */
val uiModule = module {
  single { MoneyFormatter() }
}
