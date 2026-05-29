package com.flowfin.core.domain.error

import com.flowfin.core.model.CategoryId

/**
 * Failures when creating or mutating categories. Default categories are immutable,
 * so editing or archiving one is [CannotModifyDefault]. Duplicate names are NOT an
 * error — they're allowed by design.
 */
sealed interface CategoryError {
  data object NameBlank : CategoryError
  data class NotFound(val id: CategoryId) : CategoryError
  data object CannotModifyDefault : CategoryError
  data class Unexpected(val cause: Throwable) : CategoryError
}
