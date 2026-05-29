package com.flowfin.core.domain.repository

import arrow.core.Either
import com.flowfin.core.domain.error.PersonError
import com.flowfin.core.model.Person
import com.flowfin.core.model.PersonId
import kotlinx.coroutines.flow.Flow

/** Contacts for debts. Names are unique among non-archived persons, case-insensitively. */
interface PersonRepository {

  fun observeActive(): Flow<List<Person>>

  suspend fun getById(id: PersonId): Person?

  /** Type-ahead search over active persons. */
  suspend fun search(query: String): List<Person>

  /** The active person with this exact name (case-insensitive), if any. */
  suspend fun findByName(name: String): Person?

  suspend fun create(name: String, avatarTintIndex: Int): Either<PersonError, Person>
}
