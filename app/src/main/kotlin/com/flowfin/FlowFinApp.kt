package com.flowfin

import android.app.Application
import com.flowfin.core.data.di.dataModule
import com.flowfin.core.database.databaseModule
import com.flowfin.feature.home.di.homeModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FlowFinApp : Application() {
  override fun onCreate() {
    super.onCreate()
    val koin = startKoin {
      androidLogger(Level.INFO)
      androidContext(this@FlowFinApp)
      modules(databaseModule, dataModule, homeModule)
    }.koin

    // Temporary: populate sample data until create-flows exist (see DevSeed).
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      seedSampleDataIfEmpty(koin.get(), koin.get(), koin.get(), koin.get(), koin.get())
    }
  }
}
