package com.flowfin.core.data

import app.cash.sqldelight.db.SqlDriver
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

/** The typed database plus its raw driver, for the few tests that seed via raw SQL. */
internal class TestDatabase(val db: FlowFinDatabase, val driver: SqlDriver)

/** Fresh in-memory database with foreign keys enabled, per test. */
internal fun inMemoryTestDatabase(): TestDatabase {
  val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
  FlowFinDatabase.Schema.create(driver)
  driver.execute(null, "PRAGMA foreign_keys = ON", 0)
  return TestDatabase(flowFinDatabase(driver), driver)
}

internal fun inMemoryDatabase(): FlowFinDatabase = inMemoryTestDatabase().db

/**
 * Seeds a shipped default category. The typed `insertCustom` query can only make
 * customs (is_default = 0), so we insert then flip the flag via raw SQL — this is
 * the one place tests need a default row, until first-launch seeding is built.
 */
internal fun TestDatabase.seedDefaultCategory(scope: CategoryScope, now: Instant): CategoryId {
  val id = CategoryId(Uuid.generateV7())
  db.categoriesQueries.insertCustom(id, "default-$scope", scope, null, null, 0, now, now)
  driver.execute(null, "UPDATE categories SET is_default = 1 WHERE id = ?", 1) {
    bindBytes(0, id.value.toByteArray())
  }
  return id
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
