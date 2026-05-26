# Empty states inventory

A burndown of every empty state across the app, organized by screen. Each state
is tagged with a **category** (which dictates the design treatment) and a
**status** (designed / pending / N/A).

When you start designing one, switch its status. When it's shipped, link the
mockup file from its row.

---

## Categories

The treatment changes with the category — don't reuse a takeover where a hint
is enough, and don't downgrade a takeover into a hint.

| | Category | Treatment | Voice |
|---|---|---|---|
| **F** | **Full empty** | Screen takeover. Hero is muted, primary CTA replaces the populated body, FAB hidden if its action depends on missing data. | Editorial, declarative. *"Money lives somewhere. Tell us where."* |
| **S** | **Section empty** | Inline empty hint *inside* a populated screen. Short, one mono line + optional small link. Never takes over. | Quiet, factual. *"No pending payments this week."* |
| **Q** | **Filter / search empty** | Filter UI stays visible. Brief inline notice where the results would be. | Specific to the query. *"Nothing matches "rent" — try a different month."* |
| **N** | **Not-enough-yet** | Calm, informational. No CTA — the user can't act, they just need time. Often paired with a placeholder chart or faded preview. | Patient. *"We'll surface patterns once you have a month of data."* |

## Status legend

- ✓ Designed
- · Pending
- — N/A (state can't happen given the data model)

---

## 01 · Home

The first-seen surface — empty states here are the most consequential.

| State | Cat | Status | Notes |
|---|---|---|---|
| No accounts, no transactions | **F** | ✓ [home/empty.html](../design/mockups/home/empty.html) | Skipped onboarding → land on home with zero. CTA: *Add an account*. FAB hidden. |
| Has accounts, never logged a transaction | **S** | ✓ [home/no-transactions.html](../design/mockups/home/no-transactions.html) | Hero shows real balance, Pending hidden, Recent gets the inline empty hint. Re-categorized from **F** → **S** during design: the screen isn't empty when accounts/budgets are populated. |
| Accounts + transactions, but none this period | **S** | ✓ [home/no-recent-activity.html](../design/mockups/home/no-recent-activity.html) | Hero + Accounts + Pending populated. Recent section uses the inline empty hint with copy *"Nothing logged this week."* and a 6-days-ago reference. |
| Accounts exist, no recurring schedules | **S** | ✓ [home/no-recurring.html](../design/mockups/home/no-recurring.html) | Pending section absent entirely. No inline hint — the absence itself reads correctly, and the Recurring tab covers feature discovery. |
| Has transactions but < 1 month of history | **N** | ✓ [home/early-data.html](../design/mockups/home/early-data.html) | Hero meta shows clock glyph + *"Day 5 · building a picture"* in italic, replacing the green trend %. Recent trimmed to today only. |

## 02 · Onboarding

By nature these screens are empty-state-first — they *are* the empty state of
the app. No further variants needed.

| State | Cat | Status | Notes |
|---|---|---|---|
| 1-welcome / 2-real-accounts / 3-budgets / 4-reminder | — | — | The screens themselves are the empty experience. |

---

## 03 · Accounts (tab)

**Data-model note:** Budgets are allocations from real accounts (envelope-style,
not just caps). Hero meta on populated home reads *"Allocated Rs 16,000"*, and
transactions like *"Bank → Food"* literally move money from a real account
into a budget. Therefore budgets cannot exist without at least one real
account. The system should prevent that state (cascade delete, or block
deletion of the last real account when budgets exist).

| State | Cat | Status | Notes |
|---|---|---|---|
| No real accounts, no budgets | **F** | ✓ [accounts-empty.html](../design/mockups/accounts/accounts-empty.html) | Only the Real section is shown — Budget section is hidden entirely (budgets need a real account first). Single inline hint with *Add a real account* link. Hero shows muted Rs 0. |
| Real accounts exist, no budgets | **S** | ✓ [accounts-no-budgets.html](../design/mockups/accounts/accounts-no-budgets.html) | Real cards populated, Budget segment shows inline empty hint with *Set a budget* link. Hero breakdown omits the Budget chip. |
| Budgets exist, no real accounts | — | — | System prevents this state. No screen needed. |
| No archived accounts | — | — | Hide the *Archived* row entirely; never show "0 archived." |

## 04 · Account detail — real (`accounts/detail-real.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| Account just created, no transactions yet | **F** | · | Hero balance = whatever opening balance was entered, *Activity* section is a centered empty moment. CTA: *Log a transaction*. |
| Has transactions, none this month | **S** | · | Flow strip shows Rs 0 in / Rs 0 out for the month; activity section empty for this period. Inline hint. |

## 05 · Account detail — budget (`accounts/detail.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| Budget just created, no spending yet | **F** | · | Progress bar at 0%, activity empty. Encouraging tone — they haven't *failed*, they just haven't started. |
| Budget had spending in past period, none this period | **S** | · | Show the period nav clearly; inline hint inside activity. |

---

## 06 · Transactions

`transactions/add.html` is a form — no empty state of its own.

Picker filter-empties for this form live under **08 · Pickers**.

---

## 07 · Recurring (tab)

| State | Cat | Status | Notes |
|---|---|---|---|
| No recurring schedules at all | **F** | · | Both *Pending* and *Schedule* absent. CTA: *Set up a recurring payment*. |
| Schedules exist, none pending right now | **S** | · | Hide *Pending* section, keep *Schedule* populated. No inline hint needed — the absence reads as good news. |
| All schedules paused | **S** | · | Active section empty, paused section populated. Inline hint inside Active with *Resume one →* link. |
| Has paused/archived but no current schedules | **S** | · | Edge case — inline hint same as above. |
| Stats / "Rs X monthly" with no schedules | **N** | · | If we show a "monthly total" tile, replace with placeholder *Add one to see your monthly load.* |

`recurring/add.html` is a form — no empty state.

---

## 08 · Debts (tab)

| State | Cat | Status | Notes |
|---|---|---|---|
| No debts, no receivables | **F** | · | Takeover. CTA pair: *Record a debt* / *Record a receivable* (two-button row). |
| Has *I owe*, no *Owe me* | **S** | · | Hide *Owe me* segment, keep *I owe* populated. No inline hint — absence is neutral. |
| Has *Owe me*, no *I owe* | **S** | · | Mirror — same treatment. |
| All settled (everything in *Settled*) | **F** | · | Special variant: hero says *"All clear."* with a faint check; settled section visible below. Different from no-debts-ever. |
| No settled history | — | — | Hide settled link entirely. |

## 09 · Debt detail (`debts/detail.html`, `debts/detail-receivable.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| Just added, only origin entry on the timeline | **S** | · | Timeline shows the origin dot. Inline hint below it: *No payments yet — [Record a payment →]*. |

`add-debt.html`, `add-receivable.html`, `record-payment.html` — forms, no empty states.

---

## 10 · Reports — spending (`reports/index.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| No transactions logged ever | **F** | · | Takeover. *"Reports appear after your first transaction."* CTA: *Log one*. |
| < 1 month of data | **N** | · | Trend chart renders only the days available, axis stays full month. Inline note above donut: *Building a picture — N days in.* |
| Selected a past month with no activity | **Q** | · | Month strip works normally. Body of the period reads: *Nothing logged in May 2026.* |
| Filter applied (category / account), no results | **Q** | · | Filter chip visible. Inline notice: *No spending in this category this month.* |

## 11 · Reports — income (`reports/income.html`)

Same axes as Reports — spending. Status pending for all four.

## 12 · Reports — insights (`reports/insights.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| < 1 month of data | **N** | · | Friendly *"Patterns surface after about a month of tracking."* No CTA. Optionally show a faded preview card with the insight format. |
| Exactly enough data, but no notable patterns | **N** | · | *"Nothing to flag this month — keep going."* Rare in practice, but the data model allows it. |
| No transactions at all | **F** | · | Inherits from Reports full-empty — probably route to that screen instead. |

---

## 13 · Settings (`settings/index.html`)

Settings is mostly populated by defaults; few real empty states.

| State | Cat | Status | Notes |
|---|---|---|---|
| No archived accounts | — | — | Hide *Archived accounts* row entirely. Already correct in the populated mockup. |
| No archived categories | — | — | Same — hide the row. |
| First-time backup (no prior backups) | **S** | · | *Backup* row sub-line says *Never backed up · last activity 27 May* instead of a date. |

## 14 · Categories (`settings/categories.html`)

Defaults always exist, so the screen is never fully empty.

| State | Cat | Status | Notes |
|---|---|---|---|
| Only defaults, no custom categories | **S** | · | Inline hint below the defaults list: *Tap + to add a custom category.* |
| Search yields no matches | **Q** | · | Filter empty when user types in the search box. *"No category matches 'xyz'."* |

## 15 · Pickers (`settings/pickers.html`)

`pickers.html` is a gallery, not a real screen — but the picker components
themselves get used inside forms and need their own empty handling.

| Picker | State | Cat | Status | Notes |
|---|---|---|---|---|
| Account picker | No accounts | **F** within sheet | · | Sheet body: *Add an account first* with a CTA that closes the sheet and routes to account-add. |
| Account picker | Search yields nothing | **Q** | · | Inline notice below search input. |
| Category picker | No categories | — | — | Defaults always exist. |
| Category picker | Search yields nothing | **Q** | · | Same pattern as account picker search. |
| Person picker (debts) | No contacts yet | **S** within sheet | · | First-debt scenario: just a *Add new person* row, no other entries. |
| Date picker | — | — | — | Never empty by nature. |

---

## 16 · Search (cross-app)

Not a dedicated screen yet, but mentioned in the home header. Worth deciding
the treatment before that screen is built.

| State | Cat | Status | Notes |
|---|---|---|---|
| Empty query (no input yet) | **F** within sheet | · | Recent searches OR suggestion list. Not strictly "empty." |
| Query with zero matches | **Q** | · | *"Nothing matches '{query}'."* + optional *Try a different time range* link. |
| Query is too short (< 2 chars) | — | — | Just don't fire a search; no empty state. |

---

## Summary

| Tab / surface | Total states | Designed |
|---|---|---|
| Home | 5 | 5 |
| Onboarding | 0 | — |
| Accounts | 2 | 2 |
| Account detail (real) | 2 | 0 |
| Account detail (budget) | 2 | 0 |
| Recurring | 5 | 0 |
| Debts | 4 | 0 |
| Debt detail | 1 | 0 |
| Reports — spending | 4 | 0 |
| Reports — income | 4 | 0 |
| Reports — insights | 3 | 0 |
| Settings | 1 | 0 |
| Categories | 2 | 0 |
| Pickers | 4 | 0 |
| Search | 2 | 0 |
| **Total actionable** | **41** | **7** |

---

## Suggested design order

A path that resolves the highest-traffic surfaces first and lets us reuse
patterns:

1. ~~Home — no accounts~~ ✓
2. ~~Home — has accounts, no transactions~~ ✓
3. ~~Home — no activity this week~~ ✓
4. ~~Home — no recurring schedules~~ ✓
5. ~~Home — early data (< 1 month)~~ ✓
6. ~~Accounts — no real, no budgets~~ ✓
7. ~~Accounts — real, no budgets~~ ✓
8. Recurring — no schedules *(first full-empty for a non-home tab)*
9. Debts — no debts and no receivables *(needs paired CTA pattern)*
10. Reports — no transactions ever *(first "not-enough" + "full" hybrid)*
11. Account / debt detail — just-added states *(section-empty pattern locked)*
12. Section empties across recurring *(reuse the inline-hint component)*
13. Filter / search empties *(small, batched together)*
14. Not-enough-yet for reports & insights *(unique tone, designed late)*

Once the inline-hint and not-enough-yet patterns are locked in, all the
section-empty rows collapse into pattern application — no new design needed.
