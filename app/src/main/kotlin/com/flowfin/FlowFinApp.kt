package com.flowfin

import android.app.Application
import com.flowfin.core.database.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FlowFinApp : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger(Level.INFO)
      androidContext(this@FlowFinApp)
      modules(databaseModule)
    }
  }
}
