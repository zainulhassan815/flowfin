package com.flowfin.core.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Cadence
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.DebtStatus
import com.flowfin.core.model.PersonId
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.model.RecurringStatus
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

/**
 * Column adapters wiring the typed values in `core:model` to their SQLite
 * columns. Register them when constructing the generated database
 * (`FlowFinDatabase(driver, accountsAdapter = Accounts.Adapter(...))`).
 *
 * IDs are [Uuid] in the model and 16 raw bytes on disk.
 */

val AccountIdAdapter           = idAdapter(::AccountId)            { it.value }
val CategoryIdAdapter          = idAdapter(::CategoryId)           { it.value }
val PersonIdAdapter            = idAdapter(::PersonId)             { it.value }
val DebtIdAdapter              = idAdapter(::DebtId)               { it.value }
val TransactionIdAdapter       = idAdapter(::TransactionId)        { it.value }
val RecurringScheduleIdAdapter = idAdapter(::RecurringScheduleId)  { it.value }

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

private inline fun <T : Any> idAdapter(
  crossinline wrap: (Uuid) -> T,
  crossinline unwrap: (T) -> Uuid,
): ColumnAdapter<T, ByteArray> = object : ColumnAdapter<T, ByteArray> {
  override fun decode(databaseValue: ByteArray): T = wrap(Uuid.fromByteArray(databaseValue))
  override fun encode(value: T): ByteArray = unwrap(value).toByteArray()
}
