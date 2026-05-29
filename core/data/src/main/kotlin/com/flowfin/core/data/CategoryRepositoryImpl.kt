package com.flowfin.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.database.CategoriesQueries
import com.flowfin.core.domain.error.CategoryError
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

internal class CategoryRepositoryImpl(
  private val queries: CategoriesQueries,
  private val ids: IdGenerator,
  private val clock: Clock,
  private val dispatcher: CoroutineDispatcher,
) : CategoryRepository {

  override fun observeByScope(scope: CategoryScope): Flow<List<Category>> =
    queries.selectActiveByScope(scope).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override fun observeAll(): Flow<List<Category>> =
    queries.selectAll().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toModel() } }

  override suspend fun getById(id: CategoryId): Category? = withContext(dispatcher) {
    queries.selectById(id).executeAsOneOrNull()?.toModel()
  }

  override suspend fun createCustom(
    name: String,
    scope: CategoryScope,
    icon: String?,
    color: String?,
    displayOrder: Int,
  ): Either<CategoryError, Category> = withContext(dispatcher) {
    val now = clock.now()
    val id = CategoryId(ids.next())
    Either.catch {
      queries.insertCustom(id, name, scope, icon, color, displayOrder.toLong(), now, now).value
      Category(id, name, scope, isDefault = false, icon, color, displayOrder, now, now, archivedAt = null)
    }.mapLeft { CategoryError.Unexpected(it) }
  }

  override suspend fun updateCustom(
    id: CategoryId,
    name: String,
    icon: String?,
    color: String?,
    displayOrder: Int,
  ): Either<CategoryError, Unit> = withContext(dispatcher) {
    either {
      requireCustom(id)
      Either.catch {
        queries.updateCustom(name, icon, color, displayOrder.toLong(), clock.now(), id).value
        Unit
      }.mapLeft { CategoryError.Unexpected(it) }.bind()
    }
  }

  override suspend fun archive(id: CategoryId): Either<CategoryError, Unit> = withContext(dispatcher) {
    either {
      requireCustom(id)
      val now = clock.now()
      Either.catch {
        queries.archive(archivedAt = now, updatedAt = now, id = id).value
        Unit
      }.mapLeft { CategoryError.Unexpected(it) }.bind()
    }
  }

  override suspend fun unarchive(id: CategoryId): Either<CategoryError, Unit> = withContext(dispatcher) {
    either {
      requireCustom(id)
      Either.catch {
        queries.unarchive(updatedAt = clock.now(), id = id).value
        Unit
      }.mapLeft { CategoryError.Unexpected(it) }.bind()
    }
  }

  /** Raises [CategoryError.NotFound] / [CategoryError.CannotModifyDefault] unless [id] is an existing custom. */
  private fun Raise<CategoryError>.requireCustom(id: CategoryId) {
    val row = queries.selectById(id).executeAsOneOrNull() ?: raise(CategoryError.NotFound(id))
    ensure(row.is_default == 0L) { CategoryError.CannotModifyDefault }
  }
}
