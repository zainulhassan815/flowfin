package com.flowfin.core.data

import com.flowfin.core.domain.error.CategoryError
import com.flowfin.core.domain.usecase.CreateCategory
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CategoryRepositoryTest {

  private val at = Instant.fromEpochMilliseconds(1_700_000_000_000)
  private val db = inMemoryDatabase()
  private val categories = CategoryRepositoryImpl(db.categoriesQueries, UuidV7Generator(), FixedClock(at), Dispatchers.Unconfined)
  private val createCategory = CreateCategory(categories)

  @Test
  fun `seeding ships the default categories into both scopes`() = runTest {
    categories.ensureDefaultsSeeded().rightOrFail()

    val expense = categories.observeByScope(CategoryScope.EXPENSE).first()
    val income = categories.observeByScope(CategoryScope.INCOME).first()
    assertEquals(13, expense.size)
    assertEquals(6, income.size)
    assertTrue(expense.all { it.isDefault })
    assertTrue(income.any { it.name == "Salary" })
  }

  @Test
  fun `seeding is idempotent`() = runTest {
    categories.ensureDefaultsSeeded().rightOrFail()
    categories.ensureDefaultsSeeded().rightOrFail()

    assertEquals(13, categories.observeByScope(CategoryScope.EXPENSE).first().size)
  }

  @Test
  fun `a shipped default cannot be archived`() = runTest {
    categories.ensureDefaultsSeeded().rightOrFail()
    val default = categories.observeByScope(CategoryScope.EXPENSE).first().first { it.isDefault }

    assertEquals(CategoryError.CannotModifyDefault, categories.archive(default.id).leftOrFail())
  }

  @Test
  fun `a created custom category appears only in its own scope`() = runTest {
    val food = createCategory("Food", CategoryScope.EXPENSE).rightOrFail()

    assertTrue(categories.observeByScope(CategoryScope.EXPENSE).first().any { it.id == food.id && it.isCustom })
    assertTrue(categories.observeByScope(CategoryScope.INCOME).first().none { it.id == food.id })
  }

  @Test
  fun `a blank category name is rejected`() = runTest {
    assertEquals(CategoryError.NameBlank, createCategory("  ", CategoryScope.EXPENSE).leftOrFail())
  }

  @Test
  fun `archiving a custom category hides it from its scope`() = runTest {
    val food = createCategory("Food", CategoryScope.EXPENSE).rightOrFail()

    categories.archive(food.id).rightOrFail()

    assertTrue(categories.observeByScope(CategoryScope.EXPENSE).first().none { it.id == food.id })
  }

  @Test
  fun `archiving an unknown category reports not found`() = runTest {
    val missing = CategoryId(Uuid.generateV7())

    assertEquals(CategoryError.NotFound(missing), categories.archive(missing).leftOrFail())
  }
}
