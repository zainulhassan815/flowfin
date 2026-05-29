package com.flowfin.core.data

import com.flowfin.core.domain.error.PersonError
import com.flowfin.core.domain.usecase.CreatePerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PersonRepositoryTest {

  private val at = Instant.fromEpochMilliseconds(1_700_000_000_000)
  private val db = inMemoryDatabase()
  private val persons = PersonRepositoryImpl(db.personsQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val createPerson = CreatePerson(persons)

  @Test
  fun `a created person can be found by name`() = runTest {
    val ahmed = createPerson("Ahmed").rightOrFail()

    assertEquals(ahmed.id, persons.findByName("Ahmed")?.id)
  }

  @Test
  fun `a duplicate name is rejected case-insensitively`() = runTest {
    createPerson("Ahmed").rightOrFail()

    val result = createPerson("ahmed")

    assertIs<PersonError.DuplicateName>(result.leftOrFail())
  }

  @Test
  fun `a blank name is rejected`() = runTest {
    assertEquals(PersonError.NameBlank, createPerson("   ").leftOrFail())
  }
}
