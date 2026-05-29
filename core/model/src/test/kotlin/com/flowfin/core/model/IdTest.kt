package com.flowfin.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/**
 * Ids are backed by [Uuid] specifically so they have structural value semantics.
 * A raw `ByteArray` value class compares by reference, which silently breaks set
 * and map membership — this fails if anyone reverts that decision. The ByteArray
 * round-trip the adapters rely on is covered by the repository tests, which write
 * and read an id back through real SQLite.
 */
class IdTest {

  @Test
  fun `ids backed by the same uuid are equal and usable as keys`() {
    val uuid = Uuid.generateV7()
    val a = AccountId(uuid)
    val b = AccountId(uuid)

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
    assertTrue(mapOf(a to "x").containsKey(b))
  }

  @Test
  fun `ids from different uuids are not equal`() {
    assertNotEquals(AccountId(Uuid.generateV7()), AccountId(Uuid.generateV7()))
  }
}
