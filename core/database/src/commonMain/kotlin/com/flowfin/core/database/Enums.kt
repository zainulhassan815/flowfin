package com.flowfin.core.database

/**
 * Schema-level enums. Constant names are the strings stored on disk and
 * mirror the `TEXT CHECK`-constrained columns in the `.sq` files exactly.
 */

enum class AccountType { REAL, BUDGET }

enum class CategoryScope { EXPENSE, INCOME }

enum class DebtDirection { I_OWE, OWED_TO_ME }

enum class DebtStatus { ACTIVE, SETTLED }

enum class RecurringStatus { ACTIVE, PAUSED }

enum class Cadence { WEEKLY, MONTHLY, YEARLY }

enum class TransactionKind {
  INCOME,
  EXPENSE,
  TRANSFER,
  ALLOCATION,
  REALLOCATION,
  DEBT_BORROW,
  DEBT_LEND,
  DEBT_REPAY_OUT,
  DEBT_REPAY_IN,
}
