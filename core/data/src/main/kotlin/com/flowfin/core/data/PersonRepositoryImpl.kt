package com.flowfin.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.database.PersonsQueries
import com.flowfin.core.domain.error.PersonError
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.model.Person
import com.flowfin.core.model.PersonId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

internal class PersonRepositoryImpl(
  private val queries: PersonsQueries,
  private val ids: IdGenerator,
  private val clock: Clock,
  private val dispatcher: CoroutineDispatcher,
) : PersonRepository {

  override fun observeActive(): Flow<List<Person>> =
    queries.selectActive().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override suspend fun getById(id: PersonId): Person? = withContext(dispatcher) {
    queries.selectById(id).executeAsOneOrNull()?.toModel()
  }

  override suspend fun search(query: String): List<Person> = withContext(dispatcher) {
    queries.searchByName(query).executeAsList().map { it.toModel() }
  }

  override suspend fun findByName(name: String): Person? = withContext(dispatcher) {
    queries.findByExactName(name).executeAsOneOrNull()?.toModel()
  }

  override suspend fun create(name: String, avatarTintIndex: Int): Either<PersonError, Person> = withContext(dispatcher) {
    either {
      ensure(queries.findByExactName(name).executeAsOneOrNull() == null) { PersonError.DuplicateName(name) }
      val now = clock.now()
      val id = PersonId(ids.next())
      Either.catch {
        queries.insert(id, name, avatarTintIndex.toLong(), now, now).value
      }.mapLeft { PersonError.Unexpected(it) }.bind()
      Person(id, name, avatarTintIndex, now, now, archivedAt = null)
    }
  }
}
