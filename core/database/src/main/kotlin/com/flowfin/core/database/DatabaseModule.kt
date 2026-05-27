package com.flowfin.core.database

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Koin bindings for [FlowFinDatabase] and its underlying driver. */
val databaseModule = module {
  single { flowFinDatabaseDriver(get()) }
  single { flowFinDatabase(get()) }

  // Queries: expose each table's QueriesImpl as a singleton so feature
  // modules can inject just the surface they need.
  single { get<FlowFinDatabase>().accountsQueries }
  single { get<FlowFinDatabase>().categoriesQueries }
  single { get<FlowFinDatabase>().personsQueries }
  single { get<FlowFinDatabase>().recurringSchedulesQueries }
  single { get<FlowFinDatabase>().debtsQueries }
  single { get<FlowFinDatabase>().transactionsQueries }
  single { get<FlowFinDatabase>().schemaMetaQueries }
}
