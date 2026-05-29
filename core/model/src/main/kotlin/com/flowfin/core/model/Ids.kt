package com.flowfin.core.model

import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * Strongly-typed primary keys. Each ID is a distinct type so the compiler
 * rejects passing an [AccountId] where a [CategoryId] is expected.
 *
 * Backed by [Uuid] (UUIDv7, time-ordered): structural equality and a clean
 * string form come for free, unlike a raw `ByteArray`. The database stores the
 * 16 raw bytes — `core:database`'s adapters convert via [Uuid.toByteArray] and
 * [Uuid.fromByteArray].
 */

@JvmInline value class AccountId(val value: Uuid)

@JvmInline value class CategoryId(val value: Uuid)

@JvmInline value class PersonId(val value: Uuid)

@JvmInline value class DebtId(val value: Uuid)

@JvmInline value class TransactionId(val value: Uuid)

@JvmInline value class RecurringScheduleId(val value: Uuid)
