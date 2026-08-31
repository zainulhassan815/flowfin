package com.flowfin.devtools

import org.koin.dsl.module

/**
 * Debug-only Koin module. Loaded by [DevToolsActivity] (not by the app's
 * `startKoin`), so the main application wiring stays untouched. Its definitions
 * depend on the repositories / use cases the app already registered.
 */
internal val devModule = module {
  single { DevReset(get(), get()) }
  single {
    DevScenarios(
      reset = get(),
      db = get(),
      categories = get(),
      recurring = get(),
      createRealAccount = get(),
      createBudget = get(),
      recordTransaction = get(),
      createPerson = get(),
      recordBorrow = get(),
      recordLend = get(),
      recordRepayment = get(),
      debtRepository = get(),
      clock = get(),
      zone = get(),
      dispatcher = get(),
    )
  }
}
