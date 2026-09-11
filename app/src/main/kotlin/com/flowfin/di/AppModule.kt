package com.flowfin.di

import com.flowfin.BuildConfig
import com.flowfin.feature.settings.AppVersion
import com.flowfin.lock.AppLock
import com.flowfin.notifications.Notifier
import com.flowfin.ui.FlowFinAppViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Shell-scoped dependencies that aren't owned by any one feature. */
val appModule = module {
  viewModelOf(::FlowFinAppViewModel)
  // Only the app module sees BuildConfig, so it supplies the version Settings shows.
  single { AppVersion(name = BuildConfig.VERSION_NAME, code = BuildConfig.VERSION_CODE.toString()) }
  single { Notifier(androidContext()) }
  single { AppLock() }
}
