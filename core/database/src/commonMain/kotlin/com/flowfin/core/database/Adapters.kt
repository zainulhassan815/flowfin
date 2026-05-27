package com.flowfin.core.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import kotlinx.datetime.Instant

/**
 * Column adapters wiring the typed values in [Ids] and [Enums] to their
 * SQLite columns. Register them when constructing the generated database
 * (`FlowFinDatabase(driver, accountsAdapter = Accounts.Adapter(...))`).
 */

val AccountIdAdapter           = idAdapter(::AccountId)            { it.bytes }
val CategoryIdAdapter          = idAdapter(::CategoryId)           { it.bytes }
val PersonIdAdapter            = idAdapter(::PersonId)             { it.bytes }
val DebtIdAdapter              = idAdapter(::DebtId)               { it.bytes }
val TransactionIdAdapter       = idAdapter(::TransactionId)        { it.bytes }
val RecurringScheduleIdAdapter = idAdapter(::RecurringScheduleId)  { it.bytes }

val AccountTypeAdapter     = EnumColumnAdapter<AccountType>()
val CategoryScopeAdapter   = EnumColumnAdapter<CategoryScope>()
val DebtDirectionAdapter   = EnumColumnAdapter<DebtDirection>()
val DebtStatusAdapter      = EnumColumnAdapter<DebtStatus>()
val RecurringStatusAdapter = EnumColumnAdapter<RecurringStatus>()
val CadenceAdapter         = EnumColumnAdapter<Cadence>()
val TransactionKindAdapter = EnumColumnAdapter<TransactionKind>()

val InstantAdapter: ColumnAdapter<Instant, Long> = object : ColumnAdapter<Instant, Long> {
  override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
  override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}

private inline fun <T> idAdapter(
  crossinline wrap: (ByteArray) -> T,
  crossinline unwrap: (T) -> ByteArray,
): ColumnAdapter<T, ByteArray> = object : ColumnAdapter<T, ByteArray> {
  override fun decode(databaseValue: ByteArray): T = wrap(databaseValue)
  override fun encode(value: T): ByteArray = unwrap(value)
}
