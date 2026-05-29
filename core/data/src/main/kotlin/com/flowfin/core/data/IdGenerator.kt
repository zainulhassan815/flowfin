package com.flowfin.core.data

import kotlin.uuid.Uuid

/**
 * Source of new entity ids. Wraps in an interface so tests can inject a
 * deterministic generator.
 */
fun interface IdGenerator {
  fun next(): Uuid
}

/**
 * Production generator: UUIDv7 from the Kotlin stdlib. Time-ordered and strictly
 * monotonic within the process, so inserts stay sequential in the B-tree.
 */
class UuidV7Generator : IdGenerator {
  override fun next(): Uuid = Uuid.generateV7()
}
