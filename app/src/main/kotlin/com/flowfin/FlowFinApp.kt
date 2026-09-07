package com.flowfin

import android.app.Application
import com.flowfin.core.data.di.dataModule
import com.flowfin.core.database.databaseModule
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.ui.di.uiModule
import com.flowfin.di.appModule
import com.flowfin.feature.accounts.di.accountsModule
import com.flowfin.feature.debts.di.debtsModule
import com.flowfin.feature.home.di.homeModule
import com.flowfin.feature.recurring.di.recurringModule
import com.flowfin.feature.reports.di.reportsModule
import com.flowfin.feature.settings.di.settingsModule
import com.flowfin.feature.transactions.di.transactionsModule
import com.flowfin.notifications.NotificationSchedule
import com.flowfin.notifications.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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

    // Cheap and idempotent, and the channels must exist before anything posts.
    koin.get<Notifier>().createChannels()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    scope.launch {
      // Permanent: ship the default categories so income/expense can be tagged.
      koin.get<CategoryRepository>().ensureDefaultsSeeded()
    }
    scope.launch {
      // Only the two fields that decide the schedule — the worker writes its own
      // bookkeeping back into settings, and reacting to that would have it cancel
      // and re-enqueue itself mid-run.
      koin.get<SettingsRepository>().observe()
        .map { it.notificationsEnabled to it.dailyReminderTime }
        .distinctUntilChanged()
        .collect { (enabled, at) -> NotificationSchedule.sync(this@FlowFinApp, enabled, at) }
    }
  }
}
