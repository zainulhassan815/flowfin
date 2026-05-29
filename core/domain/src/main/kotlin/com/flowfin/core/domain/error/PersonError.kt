package com.flowfin.core.domain.error

/** Failures when creating or mutating persons. */
sealed interface PersonError {
  data object NameBlank : PersonError
  data class DuplicateName(val name: String) : PersonError
  data class Unexpected(val cause: Throwable) : PersonError
}
