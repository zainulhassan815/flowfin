package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.PersonError
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.model.Person

/** Creates a contact. Names default to avatar tint 1; the repo rejects duplicates. */
class CreatePerson(
  private val persons: PersonRepository,
) {
  suspend operator fun invoke(
    name: String,
    avatarTintIndex: Int = 1,
  ): Either<PersonError, Person> = either {
    ensure(name.isNotBlank()) { PersonError.NameBlank }
    persons.create(name.trim(), avatarTintIndex).bind()
  }
}
