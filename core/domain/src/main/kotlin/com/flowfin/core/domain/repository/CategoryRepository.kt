package com.flowfin.core.domain.repository

import arrow.core.Either
import com.flowfin.core.domain.error.CategoryError
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import kotlinx.coroutines.flow.Flow

/**
 * Income/expense tags. Reads return defaults first, then customs. Mutations apply
 * only to customs; the repo rejects attempts to change a default.
 */
interface CategoryRepository {

  /** Active categories in a scope (defaults first), for the picker. */
  fun observeByScope(scope: CategoryScope): Flow<List<Category>>

  fun observeAll(): Flow<List<Category>>

  suspend fun getById(id: CategoryId): Category?

  suspend fun createCustom(
    name: String,
    scope: CategoryScope,
    icon: String?,
    color: String?,
    displayOrder: Int,
  ): Either<CategoryError, Category>

  suspend fun updateCustom(
    id: CategoryId,
    name: String,
    icon: String?,
    color: String?,
    displayOrder: Int,
  ): Either<CategoryError, Unit>

  suspend fun archive(id: CategoryId): Either<CategoryError, Unit>

  suspend fun unarchive(id: CategoryId): Either<CategoryError, Unit>
}
