package com.flowfin.devtools

import arrow.core.Either
import arrow.core.getOrElse
import com.flowfin.core.database.FlowFinDatabase
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreatePerson
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordBorrow
import com.flowfin.core.domain.usecase.RecordLend
import com.flowfin.core.domain.usecase.RecordRepayment
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Money
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringDraft
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.model.TransactionDraft
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * A named database state, mapped to the screens it lets you exercise (see
 * `docs/empty-states.md`). Running one wipes the database and seeds the state
 * through the real domain use cases, so balances and "pending" thresholds behave
 * exactly as in production.
 */
enum class DevScenario(val title: String, val blurb: String) {
  EVERYDAY(
    "Everyday — the full app",
    "Three months of an established user: 3 real accounts, 4 budgets, ~100 ledger rows across every kind, recurring, debts, custom + archived categories. The screenshot build.",
  ),
  EMPTY("Empty", "Fresh install — no accounts. Full-empty Home / Accounts."),
  ACCOUNTS_ONLY("Accounts only", "Bank + Cash + JazzCash, no activity. Account-detail Opening + first-entry hint."),
  EARLY_DAYS("Early days", "Five days in — Home's \"Day 5\" hero, Reports' partial trend and the not-enough-yet states."),
  QUIET_WEEK("Quiet week", "Two months of history, but nothing in the last ~2 weeks. Home 'quiet stretch'."),
  OVERDUE_RECURRING("Overdue recurring", "An active schedule past its due date. Home Pending (late)."),
  RECURRING("Recurring schedules", "A full mix — pending, overdue, upcoming across weekly / monthly / yearly, plus one paused."),
  RECURRING_PAUSED("Recurring — all paused", "Every schedule paused. Active section empty, paused list inline, nothing pending."),
  DEBTS("Debts", "Both directions: part-paid, untouched, off-book, overpaid, and one settled. Detail timeline + settled disclosure."),
  DEBTS_SETTLED("Debts — all clear", "Every debt settled. Hero reads \"All clear.\", both tabs empty above the settled lists."),
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
  private val recordRepayment: RecordRepayment,
  private val debtRepository: DebtRepository,
  private val clock: Clock,
  private val zone: TimeZone,
  private val dispatcher: CoroutineDispatcher,
) {

  suspend fun run(scenario: DevScenario): String = withContext(dispatcher) {
    runCatching {
      reset.wipeAll()
      when (scenario) {
        DevScenario.EMPTY -> Unit
        DevScenario.EVERYDAY -> everyday()
        DevScenario.ACCOUNTS_ONLY -> accountsOnly()
        DevScenario.EARLY_DAYS -> earlyDays()
        DevScenario.QUIET_WEEK -> quietWeek()
        DevScenario.OVERDUE_RECURRING -> overdueRecurring()
        DevScenario.RECURRING -> recurringMix()
        DevScenario.RECURRING_PAUSED -> recurringAllPaused()
        DevScenario.DEBTS -> debtsOnly()
        DevScenario.DEBTS_SETTLED -> debtsAllSettled()
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

  /**
   * The flagship state, and the one to reach for first: a user three months in,
   * with every screen carrying real-looking data at once — three real accounts,
   * four funded envelopes, every transaction kind (income / expense / transfer /
   * allocation / reallocation), recurring schedules in each bucket, debts both
   * directions, and custom plus archived categories.
   *
   * Income and funding land on the 1st of each month so Reports' month navigation
   * has three populated months behind it; day-to-day spending repeats a monthly
   * pattern with a deterministic wobble, so no two months read identically.
   */
  private suspend fun everyday() {
    categories.ensureDefaultsSeeded().bind()

    val bank = createRealAccount("Bank", openingBalance = Money(2_000_000), color = "bank", icon = "bank").bind()
    val cash = createRealAccount("Cash", openingBalance = Money(350_000), color = "cash", icon = "wallet", displayOrder = 1).bind()
    val jazz = createRealAccount("JazzCash", openingBalance = Money(120_000), color = "mobile", icon = "mobile", displayOrder = 2).bind()

    val food = createBudget("Food", bank.id, color = "food", icon = "restaurant").bind().id
    val transport = createBudget("Transport", bank.id, color = "transport", icon = "directions_bus", displayOrder = 1).bind().id
    val bills = createBudget("Bills", bank.id, color = "utilities", icon = "bolt", displayOrder = 2).bind().id
    val shopping = createBudget("Shopping", bank.id, color = "shop", icon = "shopping_bag", displayOrder = 3).bind().id

    // Two customs the user actually files against, and one they've since retired —
    // the Categories screen shows all three sections that way.
    // Icon keys come from CATEGORY_ICON_KEYS — a seeded custom should be something
    // the user could actually have picked, and every key there draws its own glyph.
    val charity = customCategory("Charity", CategoryScope.EXPENSE, icon = "spa", color = "care")
    val pets = customCategory("Pets", CategoryScope.EXPENSE, icon = "favorite", color = "health", order = 1)
    val oldGym = customCategory("Old gym", CategoryScope.EXPENSE, icon = "school", color = "edu", order = 2)
    categories.archive(oldGym).bind()

    val salary = incomeCategory("payments")
    val freelance = incomeCategory("work")
    val gift = incomeCategory("card_giftcard")

    // The repeating month. Day-of-month, where it's paid from, category, note, rupees.
    val pattern = listOf(
      Spend(3, bills, "bolt", "Electricity", 620_000),
      Spend(4, cash.id, "restaurant", "Chai and samosa", 45_000),
      Spend(5, transport, "directions_bus", "Fuel", 550_000),
      Spend(6, food, "shopping_cart", "Weekly groceries", 780_000),
      Spend(7, cash.id, "restaurant", "Breakfast", 80_000),
      Spend(8, jazz.id, "bolt", "Mobile top-up", 100_000),
      Spend(9, food, "restaurant", "Dinner out", 320_000),
      Spend(11, bills, "bolt", "Internet", 350_000),
      Spend(12, shopping, "shopping_bag", "Kurta", 450_000),
      Spend(13, transport, "directions_bus", "Careem rides", 240_000),
      Spend(14, cash.id, "shopping_cart", "Vegetables", 150_000),
      Spend(15, food, "shopping_cart", "Kirana run", 690_000),
      Spend(16, cash.id, "spa", "Haircut", 120_000),
      Spend(17, cash.id, "directions_bus", "Rickshaw", 90_000),
      Spend(18, bank.id, "favorite", "Pharmacy", 180_000),
      Spend(19, food, "restaurant", "Lunch with the team", 260_000),
      Spend(21, bills, "bolt", "Gas bill", 280_000),
      Spend(22, shopping, "shopping_bag", "Shoes", 620_000),
      Spend(23, transport, "directions_bus", "Petrol", 500_000),
      Spend(24, bank.id, "school", "Course fee", 900_000),
      Spend(25, food, "shopping_cart", "Month-end stock-up", 720_000),
      Spend(26, cash.id, "restaurant", "Street food", 60_000),
      Spend(27, bank.id, "movie", "Cinema", 300_000),
      Spend(28, jazz.id, "subscriptions", "Netflix", 150_000),
      Spend(29, cash.id, "shopping_bag", "Gift for Ammi", 200_000),
    )

    for (m in MONTHS_OF_HISTORY - 1 downTo 0) {
      // Paid, funded, and the rent gone — all on the 1st, before anything is spent.
      onDay(m, 1, hour = 9)?.let { record(TransactionDraft.Income(bank.id, Money(15_000_000), salary, "Monthly salary", it)) }
      onDay(m, 1, hour = 10)?.let { record(TransactionDraft.Allocation(bank.id, food, Money(2_800_000), it)) }
      onDay(m, 1, hour = 11)?.let { record(TransactionDraft.Allocation(bank.id, transport, Money(1_300_000), it)) }
      onDay(m, 1, hour = 12)?.let { record(TransactionDraft.Allocation(bank.id, bills, Money(1_300_000), it)) }
      onDay(m, 1, hour = 13)?.let { record(TransactionDraft.Allocation(bank.id, shopping, Money(1_100_000), it)) }
      onDay(m, 2, hour = 10)?.let { record(TransactionDraft.Expense(bank.id, Money(4_500_000), expenseCategory("home"), "Rent", it)) }
      onDay(m, 3, hour = 17)?.let { record(TransactionDraft.Transfer(bank.id, cash.id, Money(1_000_000), "ATM withdrawal", it)) }
      onDay(m, 7, hour = 10)?.let { record(TransactionDraft.Expense(bank.id, Money(200_000), charity, "Monthly donation", it)) }
      onDay(m, 8, hour = 12)?.let { record(TransactionDraft.Income(bank.id, Money(300_000), gift, "Eidi", it)) }
      onDay(m, 10, hour = 13)?.let { record(TransactionDraft.Transfer(bank.id, jazz.id, Money(300_000), "Top up wallet", it)) }
      onDay(m, 14, hour = 16)?.let { record(TransactionDraft.Income(bank.id, Money(1_800_000), freelance, "Logo work", it)) }
      onDay(m, 17, hour = 18)?.let { record(TransactionDraft.Expense(cash.id, Money(140_000), pets, "Cat food", it)) }
      onDay(m, 20, hour = 14)?.let { record(TransactionDraft.Reallocation(shopping, food, Money(200_000), it)) }

      pattern.forEach { spend ->
        // A plausible hour off the day, so a day's rows carry an order rather than
        // all landing on midnight and sorting arbitrarily.
        val at = onDay(m, spend.day, hour = 8 + spend.day % 12) ?: return@forEach
        record(
          TransactionDraft.Expense(
            from = spend.from,
            amount = wobble(spend.amount, m, spend.day),
            category = expenseCategory(spend.icon),
            note = spend.note,
            recordedAt = at,
          ),
        )
      }
    }

    // Recurring: one overdue, one due today, four upcoming across every cadence,
    // the salary on the income side, and one paused behind the disclosure.
    expenseSchedule("Netflix", Money(150_000), Recurrence.Monthly(22), bank.id, expenseCategory("subscriptions"), daysAgo(2))
    expenseSchedule("Gym membership", Money(500_000), Recurrence.Monthly(25), bank.id, expenseCategory("favorite"), daysAgo(0))
    expenseSchedule("Bus pass", Money(120_000), Recurrence.Weekly(dayOfWeek = 1), bank.id, expenseCategory("directions_bus"), daysFromNow(3))
    expenseSchedule("Rent", Money(4_500_000), Recurrence.Monthly(1), bank.id, expenseCategory("home"), daysFromNow(6))
    expenseSchedule("Internet", Money(350_000), Recurrence.Monthly(10), bank.id, expenseCategory("bolt"), daysFromNow(12))
    expenseSchedule("Domain renewal", Money(350_000), Recurrence.Yearly(month = 7, dayOfMonth = 1), bank.id, expenseCategory("shopping_bag"), daysFromNow(40))
    recurring.create(
      RecurringDraft.Income("Salary", Money(15_000_000), Recurrence.Monthly(dayOfMonth = 1), bank.id, salary),
      firstDueAt = daysFromNow(4),
    ).bind()
    val spotify = expenseSchedule("Spotify", Money(90_000), Recurrence.Monthly(15), bank.id, expenseCategory("subscriptions"), daysFromNow(9))
    recurring.pause(spotify).bind()

    seedDebts(bank.id)
    backdateHistory(days = MONTHS_OF_HISTORY * 31 + 5)
  }

  private suspend fun accountsOnly() {
    bank(Money(5_000_000))
    createRealAccount("Cash", openingBalance = Money(700_000), color = "cash", icon = "wallet", displayOrder = 1).bind()
    createRealAccount("JazzCash", openingBalance = Money(120_000), color = "mobile", icon = "mobile", displayOrder = 2).bind()
  }

  /**
   * Five days in: enough to render, not enough to conclude anything. Home's hero
   * reads "Day 5", Reports draws a partial trend, and the insights sheet stays in
   * its not-enough-yet state.
   */
  private suspend fun earlyDays() {
    categories.ensureDefaultsSeeded().bind()
    val bank = bank(Money(3_000_000))
    val cash = createRealAccount("Cash", openingBalance = Money(200_000), color = "cash", icon = "wallet", displayOrder = 1).bind()
    val food = createBudget("Food", bank.id, color = "food", icon = "restaurant").bind()

    record(TransactionDraft.Income(bank.id, Money(15_000_000), incomeCategory("payments"), "Monthly salary", daysAgo(4)))
    record(TransactionDraft.Allocation(bank.id, food.id, Money(2_500_000), daysAgo(4)))
    record(TransactionDraft.Transfer(bank.id, cash.id, Money(800_000), "ATM withdrawal", daysAgo(4)))
    record(TransactionDraft.Expense(bank.id, Money(4_500_000), expenseCategory("home"), "Rent", daysAgo(3)))
    record(TransactionDraft.Expense(food.id, Money(780_000), expenseCategory("shopping_cart"), "Groceries", daysAgo(3)))
    record(TransactionDraft.Expense(cash.id, Money(90_000), expenseCategory("directions_bus"), "Rickshaw", daysAgo(2)))
    record(TransactionDraft.Expense(food.id, Money(260_000), expenseCategory("restaurant"), "Lunch", daysAgo(1)))
    record(TransactionDraft.Expense(bank.id, Money(620_000), expenseCategory("bolt"), "Electricity", daysAgo(0)))

    // Day 1 is the oldest account's creation day, so four days back reads "Day 5".
    backdateHistory(days = 4)
  }

  /** Two months of history, then nothing for a fortnight — Home's quiet stretch. */
  private suspend fun quietWeek() {
    categories.ensureDefaultsSeeded().bind()
    val bank = bank(Money(4_000_000))
    val cash = createRealAccount("Cash", openingBalance = Money(300_000), color = "cash", icon = "wallet", displayOrder = 1).bind()
    val food = createBudget("Food", bank.id, color = "food", icon = "restaurant").bind()
    val salary = incomeCategory("payments")

    // Everything is at least a fortnight old, so this calendar week reads empty.
    for (m in 1 downTo 0) {
      onDay(m, 1)?.takeIf { it <= daysAgo(15) }?.let { at ->
        record(TransactionDraft.Income(bank.id, Money(15_000_000), salary, "Monthly salary", at))
        record(TransactionDraft.Allocation(bank.id, food.id, Money(2_500_000), at))
      }
    }
    record(TransactionDraft.Expense(bank.id, Money(4_500_000), expenseCategory("home"), "Rent", daysAgo(38)))
    record(TransactionDraft.Expense(food.id, Money(720_000), expenseCategory("shopping_cart"), "Groceries", daysAgo(34)))
    record(TransactionDraft.Expense(cash.id, Money(150_000), expenseCategory("restaurant"), "Dinner out", daysAgo(29)))
    record(TransactionDraft.Expense(bank.id, Money(620_000), expenseCategory("bolt"), "Electricity", daysAgo(24)))
    record(TransactionDraft.Expense(food.id, Money(680_000), expenseCategory("shopping_cart"), "Groceries", daysAgo(21)))
    record(TransactionDraft.Expense(bank.id, Money(300_000), expenseCategory("movie"), "Cinema", daysAgo(18)))
    record(TransactionDraft.Expense(cash.id, Money(90_000), expenseCategory("directions_bus"), "Rickshaw", daysAgo(15)))
    backdateHistory(days = 70)
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

  /** A full Recurring tab: two pending (one due today, one overdue), several
   *  upcoming across weekly / monthly / yearly, a salary income schedule, and one
   *  paused so the paused disclosure has something behind it.
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

    val spotify = expenseSchedule("Spotify", Money(90_000), Recurrence.Monthly(15), bank.id, expenseCategory("subscriptions"), daysFromNow(9))
    recurring.pause(spotify).bind()

    backdateHistory(days = 40)
  }

  /**
   * Every schedule paused: the Active section goes empty (with its "Resume one"
   * hint), the paused list surfaces inline, and nothing is pending — one seed
   * covers both designed states.
   */
  private suspend fun recurringAllPaused() {
    categories.ensureDefaultsSeeded().bind()
    val bank = bank(Money(8_000_000))

    expenseSchedule("Gym Membership", Money(500_000), Recurrence.Monthly(25), bank.id, expenseCategory("favorite"), daysFromNow(9))
    expenseSchedule("Netflix", Money(150_000), Recurrence.Monthly(22), bank.id, expenseCategory("movie"), daysFromNow(5))
    expenseSchedule("Rent", Money(3_000_000), Recurrence.Monthly(1), bank.id, expenseCategory("home"), daysFromNow(6))
    expenseSchedule("Internet", Money(250_000), Recurrence.Monthly(10), bank.id, expenseCategory("bolt"), daysFromNow(15))

    recurring.observeAll().first().forEach { recurring.pause(it.id).bind() }
    backdateHistory(days = 40)
  }

  /** The debts seed on its own account, for exercising the tab in isolation. */
  private suspend fun debtsOnly() {
    val bank = bank(Money(5_000_000))
    seedDebts(bank.id)
    backdateHistory(days = 60)
  }

  /**
   * Both directions, dense enough that one seed reaches every Debts state the
   * app can render: a part-paid debt (progress bar + multi-entry timeline), an
   * untouched one (0%, origin-only timeline), an off-book one (no account
   * movement), an overpaid one (negative remaining), and a settled one behind
   * the tab's disclosure.
   */
  private suspend fun seedDebts(account: AccountId) {
    val ali = createPerson("Ali Raza", avatarTintIndex = 1).bind()
    val sara = createPerson("Sara Khan", avatarTintIndex = 3).bind()
    val imran = createPerson("Imran", avatarTintIndex = 2).bind()
    val hina = createPerson("Hina", avatarTintIndex = 4).bind()
    val bilal = createPerson("Bilal", avatarTintIndex = 5).bind()

    // I owe · part-paid — three timeline entries, progress just past half.
    val rent = recordBorrow(ali.id, account, Money(8_000_000), "Borrowed for rent", daysAgo(21)).bind()
    repay(rent.id, account, Money(2_000_000), daysAgo(14))
    repay(rent.id, account, Money(2_500_000), daysAgo(8), note = "rent share")
    repay(rent.id, account, Money(1_000_000), daysAgo(2))

    // I owe · untouched — origin-only timeline, 0% progress, "Full amount".
    recordBorrow(imran.id, account, Money(2_500_000), reason = null, recordedAt = daysAgo(5)).bind()

    // I owe · off-book — no account movement at all, so no balance shifts.
    val offBook = recordBorrow(hina.id, intoAccount = null, amount = Money(1_200_000), reason = "Cash from Ammi", recordedAt = daysAgo(11)).bind()
    repay(offBook.id, account = null, amount = Money(400_000), at = daysAgo(3))

    // Owed to me · part-paid.
    val lunch = recordLend(sara.id, account, Money(3_000_000), "Covered the lunch tab", daysAgo(16)).bind()
    repay(lunch.id, account, Money(1_000_000), daysAgo(6))

    // Owed to me · overpaid — remaining goes negative, which is allowed.
    val tickets = recordLend(bilal.id, account, Money(600_000), "Concert tickets", daysAgo(9)).bind()
    repay(tickets.id, account, Money(750_000), daysAgo(1), note = "rounded up")

    // Settled — sits behind the "1 settled debt" disclosure on the I-owe tab.
    val phone = recordBorrow(sara.id, account, Money(1_500_000), "Phone repair", daysAgo(40)).bind()
    repay(phone.id, account, Money(1_500_000), daysAgo(30))
    debtRepository.markSettled(phone.id).bind()
  }

  /** Every debt settled — the hero reads "All clear." and both active lists are empty. */
  private suspend fun debtsAllSettled() {
    val bank = bank(Money(5_000_000))
    val ali = createPerson("Ali Raza", avatarTintIndex = 1).bind()
    val sara = createPerson("Sara Khan", avatarTintIndex = 3).bind()

    val borrowed = recordBorrow(ali.id, bank.id, Money(2_000_000), "Short-term loan", daysAgo(30)).bind()
    repay(borrowed.id, bank.id, Money(2_000_000), daysAgo(9))
    debtRepository.markSettled(borrowed.id).bind()

    val lent = recordLend(sara.id, bank.id, Money(1_000_000), "Covered lunch tab", daysAgo(25)).bind()
    repay(lent.id, bank.id, Money(1_000_000), daysAgo(4))
    debtRepository.markSettled(lent.id).bind()

    backdateHistory(days = 60)
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
  ): RecurringScheduleId =
    recurring.create(RecurringDraft.Expense(name, amount, recurrence, from, category), firstDueAt).bind().id

  private suspend fun customCategory(
    name: String,
    scope: CategoryScope,
    icon: String,
    color: String,
    order: Int = 0,
  ): CategoryId = categories.createCustom(name, scope, icon, color, order).bind().id

  /** A seeded expense category by its icon key, falling back to the first. */
  private suspend fun expenseCategory(icon: String): CategoryId = scopedCategory(CategoryScope.EXPENSE, icon)

  private suspend fun incomeCategory(icon: String? = null): CategoryId = scopedCategory(CategoryScope.INCOME, icon)

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

  /** A repayment against a seeded debt. A null [account] keeps it off-book. */
  private suspend fun repay(debt: DebtId, account: AccountId?, amount: Money, at: Instant, note: String? = null) {
    recordRepayment(debt, account, amount, at, note).bind()
  }

  /** Backdate every seeded account and category, so Home reads an established user
   *  (past its settling window) instead of "Day 1" and no category claims it was
   *  created after the rows filed under it. Debug-only direct write. */
  private fun backdateHistory(days: Int) {
    val at = daysAgo(days)
    db.devQueries.backdateAccountsCreatedAt(at)
    db.devQueries.backdateCategoriesCreatedAt(at)
    db.devQueries.alignDebtsCreatedAtToOrigin()
  }

  /**
   * Day [day] of the month [monthsAgo] calendar months back at [hour], or null when
   * that date hasn't happened yet. Calendar-anchored rather than an offset in days,
   * so each month's rows stay inside their own month however the seed is run — which
   * is what Reports' month navigation reads. Never returns a future instant.
   */
  private fun onDay(monthsAgo: Int, day: Int, hour: Int = 9): Instant? {
    val now = clock.now()
    val today = now.toLocalDateTime(zone).date
    val first = LocalDate(today.year, today.monthNumber, 1).minus(DatePeriod(months = monthsAgo))
    val lastDay = first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).dayOfMonth
    val date = LocalDate(first.year, first.monthNumber, day.coerceAtMost(lastDay))
    if (date > today) return null
    val at = date.atStartOfDayIn(zone).plus(hour, DateTimeUnit.HOUR)
    return if (at > now) now else at
  }

  /** ±8% off the pattern amount, derived from the month and day rather than a
   *  random source — so months differ from each other but a rerun doesn't. Rounded
   *  back to whole rupees: nobody records 6,142.50 for a gas bill. */
  private fun wobble(minorUnits: Long, monthsAgo: Int, day: Int): Money =
    Money((minorUnits + minorUnits / 100 * (((monthsAgo * 31 + day * 7) % 17) - 8)) / 100 * 100)

  private fun daysAgo(n: Int): Instant = clock.now().minus(n, DateTimeUnit.DAY, zone)

  private fun daysFromNow(n: Int): Instant = clock.now().plus(n, DateTimeUnit.DAY, zone)

  /** One row of the repeating monthly spend pattern. */
  private data class Spend(
    val day: Int,
    val from: AccountId,
    val icon: String,
    val note: String,
    val amount: Long,
  )
}

/** How many calendar months of history [DevScenarios] seeds for the full state. */
private const val MONTHS_OF_HISTORY = 3

/** Unwrap a seed step, aborting the scenario with a readable message on failure. */
private fun <L, R> Either<L, R>.bind(): R = getOrElse { error(it.toString()) }
