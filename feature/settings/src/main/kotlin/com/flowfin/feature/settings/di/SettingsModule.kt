package com.flowfin.feature.settings.di

import com.flowfin.feature.settings.CategoriesViewModel
import com.flowfin.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
  viewModelOf(::SettingsViewModel)
  viewModelOf(::CategoriesViewModel)
}
