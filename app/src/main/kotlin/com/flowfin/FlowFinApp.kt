package com.flowfin

import android.app.Application
import com.flowfin.core.data.di.dataModule
import com.flowfin.core.database.databaseModule
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.ui.di.uiModule
import com.flowfin.di.appModule
import com.flowfin.feature.accounts.di.accountsModule
import com.flowfin.feature.debts.di.debtsModule
import com.flowfin.feature.home.di.homeModule
import com.flowfin.feature.recurring.di.recurringModule
import com.flowfin.feature.reports.di.reportsModule
import com.flowfin.feature.settings.di.settingsModule
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
      modules(databaseModule, dataModule, uiModule, appModule, homeModule, transactionsModule, accountsModule, recurringModule, debtsModule, settingsModule, reportsModule)
    }.koin

    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      // Permanent: ship the default categories so income/expense can be tagged.
      koin.get<CategoryRepository>().ensureDefaultsSeeded()
    }
  }
}
