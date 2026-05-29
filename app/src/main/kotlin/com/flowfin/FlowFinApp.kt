package com.flowfin

import android.app.Application
import com.flowfin.core.data.di.dataModule
import com.flowfin.core.database.databaseModule
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.ui.di.uiModule
import com.flowfin.feature.home.di.homeModule
import com.flowfin.feature.transactions.di.transactionsModule
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
      modules(databaseModule, dataModule, uiModule, homeModule, transactionsModule)
    }.koin

    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      // Permanent: ship the default categories so income/expense can be tagged.
      koin.get<CategoryRepository>().ensureDefaultsSeeded()
      // Temporary: sample accounts until an Add-Account flow exists (see DevSeed).
      seedSampleAccountsIfEmpty(koin.get(), koin.get(), koin.get())
    }
  }
}
