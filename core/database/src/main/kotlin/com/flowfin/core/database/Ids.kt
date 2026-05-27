package com.flowfin.core.database

import kotlin.jvm.JvmInline

/**
 * Strongly-typed primary keys. Each ID is a distinct type so the compiler
 * rejects passing an [AccountId] where a [CategoryId] is expected.
 *
 * Backed by 16-byte UUIDv7 BLOBs. Generation lives in the data layer; this
 * module only stores the bytes.
 */

@JvmInline value class AccountId(val bytes: ByteArray)

@JvmInline value class CategoryId(val bytes: ByteArray)

@JvmInline value class PersonId(val bytes: ByteArray)

@JvmInline value class DebtId(val bytes: ByteArray)

@JvmInline value class TransactionId(val bytes: ByteArray)

@JvmInline value class RecurringScheduleId(val bytes: ByteArray)
