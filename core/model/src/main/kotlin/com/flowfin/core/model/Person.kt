package com.flowfin.core.model

import kotlinx.datetime.Instant

/** A contact money is owed to or borrowed from. Referenced by [Debt]. */
data class Person(
  val id: PersonId,
  val name: String,
  val avatarTintIndex: Int,
  val createdAt: Instant,
  val updatedAt: Instant,
  val archivedAt: Instant?,
) {
  val isArchived: Boolean get() = archivedAt != null
}
