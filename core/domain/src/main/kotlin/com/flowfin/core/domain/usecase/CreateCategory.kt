package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.CategoryError
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryScope

/** Creates a custom category. Duplicate names are allowed, so only blankness is rejected. */
class CreateCategory(
  private val categories: CategoryRepository,
) {
  suspend operator fun invoke(
    name: String,
    scope: CategoryScope,
    icon: String? = null,
    color: String? = null,
    displayOrder: Int = 0,
  ): Either<CategoryError, Category> = either {
    ensure(name.isNotBlank()) { CategoryError.NameBlank }
    categories.createCustom(name.trim(), scope, icon, color, displayOrder).bind()
  }
}
