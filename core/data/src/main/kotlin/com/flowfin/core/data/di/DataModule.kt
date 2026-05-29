package com.flowfin.core.data.di

import com.flowfin.core.data.AccountRepositoryImpl
import com.flowfin.core.data.IdGenerator
import com.flowfin.core.data.TransactionRepositoryImpl
import com.flowfin.core.data.UuidV7Generator
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.koin.dsl.module

/**
 * Repository implementations and the use cases over them. Depends on
 * `databaseModule` for the per-table `*Queries` singletons.
 */
val dataModule = module {
  single<Clock> { Clock.System }
  single<IdGenerator> { UuidV7Generator() }
  single<CoroutineDispatcher> { Dispatchers.IO }

  single<AccountRepository> { AccountRepositoryImpl(get(), get(), get(), get()) }
  single<TransactionRepository> { TransactionRepositoryImpl(get(), get(), get(), get()) }

  factory { RecordTransaction(get(), get()) }
  factory { CreateRealAccount(get()) }
  factory { CreateBudget(get()) }
}
