package com.flowfin.core.model

import kotlinx.datetime.Instant

/**
 * A tag for income or expense rows. Defaults are shipped, immutable, and can't be
 * archived; customs are user-created, editable and archivable. Names aren't unique
 * by design — a user may create a custom "Food" alongside a default one.
 */
data class Category(
  val id: CategoryId,
  val name: String,
  val scope: CategoryScope,
  val isDefault: Boolean,
  val icon: String?,
  val color: String?,
  val displayOrder: Int,
  val createdAt: Instant,
  val updatedAt: Instant,
  val archivedAt: Instant?,
) {
  val isCustom: Boolean get() = !isDefault
  val isArchived: Boolean get() = archivedAt != null
}

/**
 * How much a category is actually used — the count shown on its row, and when it
 * last carried a transaction. Absent from the map entirely when a category has
 * never been used, rather than present with a zero.
 */
data class CategoryUsage(
  val transactionCount: Int,
  val lastUsedAt: Instant?,
)

/** One category's share of a period — the Reports breakdown row. */
data class CategoryTotal(
  val categoryId: CategoryId,
  val total: Money,
  val transactionCount: Int,
)
