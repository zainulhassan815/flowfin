package com.flowfin.devtools

import arrow.core.Either
import arrow.core.getOrElse
import com.flowfin.core.database.FlowFinDatabase
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreatePerson
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordBorrow
import com.flowfin.core.domain.usecase.RecordLend
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringDraft
import com.flowfin.core.model.TransactionDraft
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * A named database state, mapped to the screens it lets you exercise (see
 * `docs/empty-states.md`). Running one wipes the database and seeds the state
 * through the real domain use cases, so balances and "pending" thresholds behave
 * exactly as in production.
 */
enum class DevScenario(val title: String, val blurb: String) {
  EMPTY("Empty", "Fresh install — no accounts. Full-empty Home / Accounts."),
  ACCOUNTS_ONLY("Accounts only", "Bank + Cash, no activity. Account-detail Opening + first-entry hint."),
  LOADED_MONTH("Loaded month", "Accounts, budgets, salary + spending this month. Home full, flow strip."),
  QUIET_WEEK("Quiet week", "History, but nothing in the last ~2 weeks. Home 'quiet stretch'."),
  OVERDUE_RECURRING("Overdue recurring", "An active schedule past its due date. Home Pending (late)."),
  RECURRING("Recurring schedules", "A full mix — pending, overdue, and upcoming across weekly / monthly / yearly. Recurring tab."),
  DEBTS("Debts", "One I-owe + one owed-to-me."),
  NEGATIVE_BALANCE("Negative balance", "Spent more than the account holds. Warning treatment."),
  OVERSPENT_BUDGET("Over-spent budget", "Envelope spend exceeds its funding. Progress clamp."),
}

/**
 * Debug-only scenario seeding. Each [run] wipes first, then builds the state via
 * the domain use cases. Rows are dated relative to "now" so time-sensitive states
 * (pending / this-week / this-month) stay valid whenever the scenario is run.
 */
internal class DevScenarios(
  private val reset: DevReset,
  private val db: FlowFinDatabase,
  private val categories: CategoryRepository,
  private val recurring: RecurringRepository,
  private val createRealAccount: CreateRealAccount,
  private val createBudget: CreateBudget,
  private val recordTransaction: RecordTransaction,
  private val createPerson: CreatePerson,
  private val recordBorrow: RecordBorrow,
  private val recordLend: RecordLend,
  private val clock: Clock,
  private val zone: TimeZone,
  private val dispatcher: CoroutineDispatcher,
) {

  suspend fun run(scenario: DevScenario): String = withContext(dispatcher) {
    runCatching {
      reset.wipeAll()
      when (scenario) {
        DevScenario.EMPTY -> Unit
        DevScenario.ACCOUNTS_ONLY -> accountsOnly()
        DevScenario.LOADED_MONTH -> loadedMonth()
        DevScenario.QUIET_WEEK -> quietWeek()
        DevScenario.OVERDUE_RECURRING -> overdueRecurring()
        DevScenario.RECURRING -> recurringMix()
        DevScenario.DEBTS -> debts()
        DevScenario.NEGATIVE_BALANCE -> negativeBalance()
        DevScenario.OVERSPENT_BUDGET -> overspentBudget()
      }
      "Seeded — ${scenario.title}"
    }.getOrElse { "Failed: ${it.message}" }
  }

  suspend fun wipe(): String = withContext(dispatcher) {
    runCatching { reset.wipeAll(); "Wiped all data" }.getOrElse { "Failed: ${it.message}" }
  }

  suspend fun reseedCategories(): String = withContext(dispatcher) {
    categories.ensureDefaultsSeeded().fold({ "Failed: $it" }, { "Reseeded default categories" })
  }

  private suspend fun accountsOnly() {
    bank(Money(5_000_000))
    createRealAccount("Cash", openingBalance = Money(700_000), color = "cash", icon = "wallet").bind()
  }

  private suspend fun loadedMonth() {
    val (income, expense) = seedCategories()
    val bank = bank(Money(5_000_000))
    val cash = createRealAccount("Cash", openingBalance = Money(700_000), color = "cash", icon = "wallet").bind()
    val food = createBudget("Food", bank.id, color = "food", icon = "restaurant").bind()
    val transport = createBudget("Transport", bank.id, color = "transport", icon = "directions_bus").bind()

    record(TransactionDraft.Income(bank.id, Money(15_000_000), income, "Monthly salary", daysAgo(6)))
    record(TransactionDraft.Allocation(bank.id, food.id, Money(1_600_000), daysAgo(6)))
    record(TransactionDraft.Allocation(bank.id, transport.id, Money(600_000), daysAgo(6)))
    record(TransactionDraft.Transfer(bank.id, cash.id, Money(500_000), "ATM withdrawal", daysAgo(4)))
    record(TransactionDraft.Expense(bank.id, Money(1_200_000), expense, "Rent", daysAgo(5)))
    record(TransactionDraft.Expense(food.id, Money(650_000), expense, "Groceries", daysAgo(3)))
    record(TransactionDraft.Expense(transport.id, Money(300_000), expense, "Fuel", daysAgo(2)))
    record(TransactionDraft.Expense(cash.id, Money(150_000), expense, "Coffee", daysAgo(1)))
    // An established user, so Home shows the month trend rather than "Day 1".
    backdateAccounts(days = 40)
  }

  private suspend fun quietWeek() {
    val (income, expense) = seedCategories()
    val bank = bank(Money(4_000_000))
    // Newest entry is ~2 weeks old, so this calendar week reads empty.
    record(TransactionDraft.Income(bank.id, Money(15_000_000), income, "Salary", daysAgo(20)))
    record(TransactionDraft.Expense(bank.id, Money(900_000), expense, "Groceries", daysAgo(14)))
    backdateAccounts(days = 45)
  }

  private suspend fun overdueRecurring() {
    val (_, expense) = seedCategories()
    val bank = bank(Money(5_000_000))
    // Stamp the first due date in the past directly — the create use case would
    // anchor it in the future, which is never overdue.
    recurring.create(
      RecurringDraft.Expense(
        name = "Netflix",
        amount = Money(150_000),
        recurrence = Recurrence.Monthly(dayOfMonth = 1),
        fromAccount = bank.id,
        category = expense,
      ),
      firstDueAt = daysAgo(3),
    ).bind()
  }

  /** A full Recurring tab: two pending (one due today, one overdue) and several
   *  upcoming across weekly / monthly / yearly, plus a salary income schedule.
   *  `firstDueAt` is stamped directly so each lands in the intended bucket. */
  private suspend fun recurringMix() {
    categories.ensureDefaultsSeeded().bind()
    val bank = bank(Money(8_000_000))

    // Pending — due now / overdue (a past firstDueAt anchors them as pending).
    expenseSchedule("Gym Membership", Money(500_000), Recurrence.Monthly(25), bank.id, expenseCategory("favorite"), daysAgo(0))
    expenseSchedule("Netflix", Money(150_000), Recurrence.Monthly(22), bank.id, expenseCategory("movie"), daysAgo(3))

    // Upcoming — future dues across every cadence.
    expenseSchedule("Bus pass", Money(120_000), Recurrence.Weekly(dayOfWeek = 1), bank.id, expenseCategory("directions_bus"), daysFromNow(3))
    expenseSchedule("Rent", Money(3_000_000), Recurrence.Monthly(1), bank.id, expenseCategory("home"), daysFromNow(6))
    expenseSchedule("Internet", Money(250_000), Recurrence.Monthly(10), bank.id, expenseCategory("bolt"), daysFromNow(15))
    expenseSchedule("Cloud storage", Money(350_000), Recurrence.Yearly(month = 7, dayOfMonth = 1), bank.id, expenseCategory("shopping_bag"), daysFromNow(36))

    recurring.create(
      RecurringDraft.Income(
        name = "Salary",
        amount = Money(15_000_000),
        recurrence = Recurrence.Monthly(dayOfMonth = 1),
        toAccount = bank.id,
        category = incomeCategory(),
      ),
      firstDueAt = daysFromNow(4),
    ).bind()

    backdateAccounts(days = 40)
  }

  private suspend fun debts() {
    val bank = bank(Money(5_000_000))
    val ali = createPerson("Ali").bind()
    val sara = createPerson("Sara").bind()
    recordBorrow(ali.id, intoAccount = bank.id, amount = Money(2_000_000), reason = "Short-term loan", recordedAt = daysAgo(7)).bind()
    recordLend(sara.id, fromAccount = bank.id, amount = Money(1_000_000), reason = "Covered lunch tab", recordedAt = daysAgo(4)).bind()
  }

  private suspend fun negativeBalance() {
    val (_, expense) = seedCategories()
    val bank = createRealAccount("Bank", openingBalance = Money(100_000), color = "bank", icon = "bank").bind()
    // Spend past the opening balance — balance goes negative (allowed, warns).
    record(TransactionDraft.Expense(bank.id, Money(500_000), expense, "Emergency repair", daysAgo(1)))
  }

  private suspend fun overspentBudget() {
    val (_, expense) = seedCategories()
    val bank = bank(Money(5_000_000))
    val food = createBudget("Food", bank.id, color = "food", icon = "restaurant").bind()
    record(TransactionDraft.Allocation(bank.id, food.id, Money(1_000_000), daysAgo(5)))
    // Spend more than the envelope was funded — progress clamps at full.
    record(TransactionDraft.Expense(food.id, Money(1_500_000), expense, "Dining out", daysAgo(2)))
  }

  private suspend fun bank(opening: Money): Account =
    createRealAccount("Bank", openingBalance = opening, color = "bank", icon = "bank").bind()

  private suspend fun expenseSchedule(
    name: String,
    amount: Money,
    recurrence: Recurrence,
    from: AccountId,
    category: CategoryId,
    firstDueAt: Instant,
  ) {
    recurring.create(RecurringDraft.Expense(name, amount, recurrence, from, category), firstDueAt).bind()
  }

  /** A seeded expense category by its icon key, falling back to the first. */
  private suspend fun expenseCategory(icon: String): CategoryId = scopedCategory(CategoryScope.EXPENSE, icon)

  private suspend fun incomeCategory(): CategoryId = scopedCategory(CategoryScope.INCOME, icon = null)

  private suspend fun scopedCategory(scope: CategoryScope, icon: String?): CategoryId {
    val all = categories.observeByScope(scope).first()
    val match = icon?.let { key -> all.firstOrNull { it.icon == key } }
    return (match ?: all.firstOrNull() ?: error("no $scope category seeded")).id
  }

  /** Seeds the shipped defaults and returns one income + one expense category id. */
  private suspend fun seedCategories(): Pair<CategoryId, CategoryId> {
    categories.ensureDefaultsSeeded().bind()
    val income = categories.observeByScope(CategoryScope.INCOME).first().firstOrNull()
      ?: error("no income category seeded")
    val expense = categories.observeByScope(CategoryScope.EXPENSE).first().firstOrNull()
      ?: error("no expense category seeded")
    return income.id to expense.id
  }

  private suspend fun record(draft: TransactionDraft) {
    recordTransaction(draft).bind()
  }

  /** Backdate every seeded account's creation, so Home reads an established user
   *  (past its settling window) instead of "Day 1". Debug-only direct write. */
  private fun backdateAccounts(days: Int) {
    db.devQueries.backdateAccountsCreatedAt(daysAgo(days))
  }

  private fun daysAgo(n: Int): Instant = clock.now().minus(n, DateTimeUnit.DAY, zone)

  private fun daysFromNow(n: Int): Instant = clock.now().plus(n, DateTimeUnit.DAY, zone)
}

/** Unwrap a seed step, aborting the scenario with a readable message on failure. */
private fun <L, R> Either<L, R>.bind(): R = getOrElse { error(it.toString()) }
