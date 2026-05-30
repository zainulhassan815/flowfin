package com.flowfin.core.ui

import com.flowfin.core.designsystem.component.TransactionKind
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.TransactionId

/**
 * Display models shared by feature ViewModels. Money is already formatted to
 * strings; `iconKey`/`colorKey` are resolved to icons/colors by the screen via
 * [categoryIcon] / [categoryColor]. [TxRowUi.kind] is the design-system row kind
 * (the 3-way colour bucket), already mapped from the domain kind. [TxRowUi.name] is
 * a [UiText] because it may be either a user-named category or an app-owned kind
 * label (Transfer, Allocation, …) that has to come from resources.
 */

data class AccountCardUi(
  val id: AccountId,
  val name: String,
  val meta: String,
  val balanceWhole: String,
  val balanceDecimal: String,
  val iconKey: String?,
  val colorKey: String?,
  val isBudget: Boolean,
)

data class TxRowUi(
  val id: TransactionId,
  val name: UiText,
  val meta: String,
  val amount: String,
  val decimal: String,
  val kind: TransactionKind,
  val iconKey: String?,
  val colorKey: String?,
)
