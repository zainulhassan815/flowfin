package com.flowfin.core.data.di

import com.flowfin.core.data.AccountRepositoryImpl
import com.flowfin.core.data.CategoryRepositoryImpl
import com.flowfin.core.data.DebtRepositoryImpl
import com.flowfin.core.data.IdGenerator
import com.flowfin.core.data.PersonRepositoryImpl
import com.flowfin.core.data.RecurringRepositoryImpl
import com.flowfin.core.data.SettingsRepositoryImpl
import com.flowfin.core.data.UserSettingsSerializer
import com.flowfin.core.data.TransactionRepositoryImpl
import com.flowfin.core.data.UuidV7Generator
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateCategory
import com.flowfin.core.domain.usecase.CreatePerson
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.CreateRecurringSchedule
import com.flowfin.core.domain.usecase.FireSchedule
import com.flowfin.core.domain.usecase.RecordBorrow
import com.flowfin.core.domain.usecase.RecordLend
import com.flowfin.core.domain.usecase.RecordRepayment
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.domain.usecase.SkipSchedule
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.flowfin.core.model.UserSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Repository implementations and the use cases over them. Depends on
 * `databaseModule` for the per-table `*Queries` singletons.
 */
val dataModule = module {
  single<Clock> { Clock.System }
  single { TimeZone.currentSystemDefault() }
  single<IdGenerator> { UuidV7Generator() }
  single<CoroutineDispatcher> { Dispatchers.IO }

  single<AccountRepository> { AccountRepositoryImpl(get(), get(), get(), get()) }
  single<TransactionRepository> { TransactionRepositoryImpl(get(), get(), get(), get()) }
  single<PersonRepository> { PersonRepositoryImpl(get(), get(), get(), get()) }
  single<DebtRepository> { DebtRepositoryImpl(get(), get(), get(), get()) }
  single<CategoryRepository> { CategoryRepositoryImpl(get(), get(), get(), get()) }
  // Preferences live in their own file, not the database — see SettingsRepository.
  single<DataStore<UserSettings>> {
    DataStoreFactory.create(serializer = UserSettingsSerializer) {
      androidContext().dataStoreFile("user_settings.json")
    }
  }
  single<SettingsRepository> { SettingsRepositoryImpl(get()) }
  single<RecurringRepository> { RecurringRepositoryImpl(get(), get(), get(), get()) }

  factory { RecordTransaction(get(), get(), get()) }
  factory { CreateRealAccount(get()) }
  factory { CreateBudget(get()) }
  factory { CreatePerson(get()) }
  factory { CreateCategory(get()) }
  factory { RecordBorrow(get(), get(), get()) }
  factory { RecordLend(get(), get(), get()) }
  factory { RecordRepayment(get(), get()) }
  factory { CreateRecurringSchedule(get(), get(), get(), get(), get()) }
  factory { FireSchedule(get(), get()) }
  factory { SkipSchedule(get(), get()) }
}
