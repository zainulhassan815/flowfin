package com.flowfin.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import arrow.core.Either
import com.flowfin.core.database.FlowFinDatabase
import com.flowfin.core.database.flowFinDatabase
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

/** A clock frozen at a fixed instant, so timestamps are deterministic in tests. */
class FixedClock(private val instant: Instant) : Clock {
  override fun now(): Instant = instant
}

/** Fresh in-memory database with foreign keys enabled, per test. */
internal fun inMemoryDatabase(): FlowFinDatabase {
  val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
  FlowFinDatabase.Schema.create(driver)
  driver.execute(null, "PRAGMA foreign_keys = ON", 0)
  return flowFinDatabase(driver)
}

/** Seeds a category directly (the Categories aggregate isn't built yet). */
internal fun FlowFinDatabase.seedCategory(scope: CategoryScope, now: Instant): CategoryId {
  val id = CategoryId(Uuid.generateV7())
  categoriesQueries.insertCustom(
    id = id,
    name = "category-$scope-${id.value}",
    scope = scope,
    icon = null,
    color = null,
    display_order = 0,
    created_at = now,
    updated_at = now,
  )
  return id
}

internal fun <A, B> Either<A, B>.rightOrFail(): B = when (this) {
  is Either.Right -> value
  is Either.Left -> error("expected Right but was Left($value)")
}

internal fun <A, B> Either<A, B>.leftOrFail(): A = when (this) {
  is Either.Left -> value
  is Either.Right -> error("expected Left but was Right($value)")
}
