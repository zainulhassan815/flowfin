# Product Requirements Document (PRD)
# FlowFin - Personal Expense Tracker

**Version:** 1.0  
**Last Updated:** December 27, 2025  
**Status:** Draft  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Problem Statement](#2-problem-statement)
3. [Solution Overview](#3-solution-overview)
4. [Core Philosophy](#4-core-philosophy)
5. [Target Users](#5-target-users)
6. [Feature Requirements](#6-feature-requirements)
7. [Data Model](#7-data-model)
8. [User Flows](#8-user-flows)
9. [Screen Specifications](#9-screen-specifications)
10. [Technical Requirements](#10-technical-requirements)
11. [Success Metrics](#11-success-metrics)
12. [Release Plan](#12-release-plan)
13. [AI Features Strategy](#13-ai-features-strategy)
14. [Future Roadmap](#14-future-roadmap)
15. [Open Questions](#15-open-questions)
16. [Appendix](#16-appendix)

---

## 1. Executive Summary

### 1.1 Product Vision

FlowFin is a personal expense tracking application that mirrors real-world money flow through a unified account-based system. Unlike traditional budgeting apps that separate "where money is" from "what it's for," FlowFin treats everything as an account — making personal finance intuitive, traceable, and complete.

### 1.2 Key Value Proposition

> **"Fund your budgets first. Spend only from funded accounts. Know exactly what's left."**

The app provides:
- **Complete audit trail** — Every money movement is explicitly recorded
- **Simple mental model** — Everything is an account with a balance
- **Reality-mirrored tracking** — Transactions reflect how money actually flows
- **Comprehensive coverage** — Income, expenses, transfers, recurring payments, and debts in one system

### 1.3 MVP Scope

The MVP delivers a fully functional expense tracking system with:
- Real and Budget account management
- Income, expense, and transfer recording
- Recurring payment management with pending/paid workflow
- Loan and debt tracking with partial payment support
- Monthly reports with category breakdown
- Daily expense entry reminders

---

## 2. Problem Statement

### 2.1 Current Pain Points

**With passive tracking apps (Mint, Personal Capital):**
- Auto-categorization happens after spending — no behavior change
- Users watch themselves overspend without intervention
- "Where did my money go?" but no answer to "Do I have money for this?"

**With active budgeting apps (YNAB, Goodbudget):**
- Complex mental models with multiple concepts (accounts vs categories vs envelopes)
- Expensive subscriptions ($100+/year)
- Steep learning curves with philosophies and rules to internalize
- Poor support for regions without bank sync

**With manual spreadsheets:**
- High friction for daily entry
- Easy to fall behind and abandon
- No mobile accessibility
- No structured reports

### 2.2 Gap in the Market

| Need | Current Solutions | Gap |
|------|-------------------|-----|
| Simple mental model | YNAB has 4 rules + multiple concepts | Single unified concept needed |
| Complete money trail | Most apps don't track transfers explicitly | Full audit trail missing |
| Debt tracking | Afterthought in most apps | First-class feature needed |
| Recurring payments | Category targets or auto-deduct | Pending + confirm workflow needed |
| Affordability | $80-120/year subscriptions | Free/cheap alternative needed |
| Works offline/globally | Bank sync dependent | Manual-first approach needed |

### 2.3 User Problem Statement

> "I want to know exactly where my money went, how much I have left for each purpose, and be in complete control of my spending — without learning complex systems or paying expensive subscriptions."

---

## 3. Solution Overview

### 3.1 Core Concept

**Everything is an account with a balance.**

Money can only:
1. **Come in** — Income flows into Real accounts
2. **Move between** — Transfers move money between any accounts
3. **Go out** — Expenses reduce account balances

This mirrors real-world money flow:
```
Salary → Bank → ATM Withdrawal → Cash → Buy Groceries
        ↓
        → Transfer → Food Budget → Mess Expense
```

### 3.2 Account Types

| Type | Purpose | Examples |
|------|---------|----------|
| **Real Account** | Where money physically exists | Bank, Cash, Wallet, JazzCash, EasyPaisa |
| **Budget Account** | Allocated funds for specific purposes | Food Budget, Transport Budget, Entertainment |

### 3.3 The Fundamental Rule

> **You can only spend what's in the account.**

This creates natural budget discipline:
- Budget accounts start at zero
- User must transfer money from Real accounts to fund them
- Spending from a Budget account shows remaining balance instantly
- Empty account = no more spending on that category

### 3.4 How It Differs

| Aspect | Traditional Apps | FlowFin |
|--------|------------------|---------|
| Mental model | Categories + Accounts (separate) | Everything is an account |
| Budget tracking | Abstract limits | Concrete balances |
| Question answered | "Am I over budget?" | "Do I have money in this account?" |
| ATM withdrawal | Just categorize as "Cash" | Transfer: Bank → Cash (explicit) |
| Audit trail | Partial | Complete |

---

## 4. Core Philosophy

### 4.1 Design Principles

1. **Mirror Reality**
   - Transactions reflect actual money movement
   - If cash moved from bank to wallet, record a transfer
   - If you spent from your food allocation, expense from Food Budget

2. **Complete Traceability**
   - Opening the app after a week should tell the complete story
   - Every transaction answers: What? From where? To where? When?
   - Reports reconstruct the full picture

3. **Explicit Over Implicit**
   - No auto-categorization magic
   - No hidden calculations
   - User knows exactly what each transaction means

4. **Friction vs Clarity Tradeoff**
   - Accept slightly more entry effort for complete clarity
   - Optimize UX to reduce friction without sacrificing explicitness

5. **Offline-First**
   - Works without internet
   - No bank sync dependency
   - Manual entry is the primary method

### 4.2 What We Are NOT Building

- ❌ A bank sync aggregator
- ❌ An investment tracker
- ❌ A net worth calculator
- ❌ An AI-powered auto-categorizer (MVP)
- ❌ A shared/family finance tool (MVP)

### 4.3 Success Criteria

The app succeeds if users can:
- Set up their accounts in under 5 minutes
- Log a daily expense in under 10 seconds
- Answer "How much do I have left for food?" instantly
- Understand their monthly spending in one glance
- Track debts without separate apps/spreadsheets

---

## 5. Target Users

### 5.1 Primary Persona: The Conscious Spender

**Name:** Ali, 26  
**Occupation:** Software Developer  
**Location:** Lahore, Pakistan  

**Financial Situation:**
- Monthly salary: 150,000 PKR
- Uses bank account + JazzCash + cash
- Pays for monthly mess, transport, utilities
- Occasionally lends/borrows money from friends
- Has recurring subscriptions (Netflix, Spotify)

**Pain Points:**
- "I never know where my salary went by month end"
- "I forget to track expenses for days, then give up"
- "I borrowed 5000 from Ahmed but can't remember if I paid back"
- "YNAB is too expensive and complicated"
- "Bank apps don't work well for budgeting"

**Goals:**
- Know exactly how much is left for food/entertainment each month
- Track all cash transactions (very common in Pakistan)
- Keep tabs on money lent to friends
- Build a saving habit

**Quote:**
> "I just want to see: I have 8000 left for food, 2000 for transport. Simple."

### 5.2 Secondary Persona: The Debt-Conscious Student

**Name:** Sara, 22  
**Occupation:** University Student  
**Location:** Karachi, Pakistan  

**Financial Situation:**
- Monthly allowance: 25,000 PKR
- Mostly cash-based spending
- Frequently splits bills with friends
- Owes/owed small amounts regularly

**Pain Points:**
- "I can't track who owes me what"
- "My money disappears on small purchases"
- "I need something simple, not a finance degree"

**Goals:**
- Track daily small expenses (chai, rickshaw, food)
- Manage debts with friends clearly
- Stay within monthly allowance

### 5.3 Tertiary Persona: The Returnee Budgeter

**Name:** Hassan, 34  
**Occupation:** Freelancer  
**Location:** Islamabad, Pakistan  

**Financial Situation:**
- Variable monthly income (freelance)
- Previously used YNAB, found it expensive
- Multiple income sources in different currencies
- Wants envelope-style budgeting without complexity

**Pain Points:**
- "YNAB costs too much for Pakistan"
- "I understand zero-based budgeting but need a simpler tool"
- "Variable income makes budgeting harder"

**Goals:**
- Allocate variable income to budget categories
- Track business vs personal expenses
- Simple solution that works offline

---

## 6. Feature Requirements

### 6.1 Accounts

#### 6.1.1 Description
Users can create multiple accounts representing both real money locations and budget allocations.

#### 6.1.2 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| ACC-01 | User can create a new account with name and type (Real/Budget) | Must Have |
| ACC-02 | User can set initial balance when creating account | Must Have |
| ACC-03 | User can view list of all accounts with current balances | Must Have |
| ACC-04 | User can view transaction history for a specific account | Must Have |
| ACC-05 | User can edit account name | Must Have |
| ACC-06 | User can archive an account (hide from main list, preserve history) | Must Have |
| ACC-07 | User can view archived accounts separately | Should Have |
| ACC-08 | User can unarchive an account | Should Have |
| ACC-09 | Accounts with negative balance show warning indicator | Must Have |
| ACC-10 | Real accounts are visually distinguished from Budget accounts | Must Have |
| ACC-11 | User can reorder accounts in the list | Could Have |
| ACC-12 | User can set an icon/color for each account | Could Have |

#### 6.1.3 Business Rules

- Account names must be unique
- Account type cannot be changed after creation
- Accounts cannot be deleted, only archived (to preserve transaction history)
- Archived accounts don't appear in dropdowns for new transactions
- Negative balances are allowed but highlighted as warnings

#### 6.1.4 Default Accounts (First Launch)

Suggest but don't force:
- Bank (Real)
- Cash (Real)
- Food Budget (Budget)
- Transport Budget (Budget)

---

### 6.2 Income

#### 6.2.1 Description
Users can record money coming into their Real accounts.

#### 6.2.2 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| INC-01 | User can add income with amount, destination account, date | Must Have |
| INC-02 | Income can only go to Real accounts | Must Have |
| INC-03 | User can select income category | Must Have |
| INC-04 | User can add optional note to income | Must Have |
| INC-05 | Date defaults to today but can be changed | Must Have |
| INC-06 | User can edit income entry | Must Have |
| INC-07 | User can delete income entry (with confirmation) | Must Have |

#### 6.2.3 Default Income Categories

- Salary
- Freelance
- Business
- Gift
- Refund
- Other

#### 6.2.4 Business Rules

- Income must have positive amount
- Income increases the balance of destination account
- Editing income recalculates account balance
- Deleting income recalculates account balance

---

### 6.3 Transfers

#### 6.3.1 Description
Users can move money between any accounts to reflect ATM withdrawals, budget allocations, or rebalancing.

#### 6.3.2 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| TRF-01 | User can transfer money between any two accounts | Must Have |
| TRF-02 | Transfer records: amount, from account, to account, date | Must Have |
| TRF-03 | User can add optional note to transfer | Must Have |
| TRF-04 | Date defaults to today but can be changed | Must Have |
| TRF-05 | Transfer appears in history of both accounts | Must Have |
| TRF-06 | User can edit transfer | Must Have |
| TRF-07 | User can delete transfer (with confirmation) | Must Have |
| TRF-08 | Quick transfer shortcuts for common routes (e.g., Bank → Cash) | Should Have |

#### 6.3.3 Business Rules

- From and To accounts must be different
- Transfer decreases From account balance and increases To account balance
- Transfer is a single entry, not two separate transactions
- System warns if From account would go negative

---

### 6.4 Expenses

#### 6.4.1 Description
Users can record money going out from any account.

#### 6.4.2 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| EXP-01 | User can add expense with amount, source account, category, date | Must Have |
| EXP-02 | Expenses can come from any account (Real or Budget) | Must Have |
| EXP-03 | User can add optional note to expense | Must Have |
| EXP-04 | Date defaults to today but can be changed | Must Have |
| EXP-05 | User can edit expense | Must Have |
| EXP-06 | User can delete expense (with confirmation) | Must Have |
| EXP-07 | Quick expense shortcuts for frequent transactions | Should Have |
| EXP-08 | Category suggestions based on selected account | Should Have |

#### 6.4.3 Default Expense Categories

- Food & Dining
- Groceries
- Transport
- Utilities
- Rent
- Shopping
- Entertainment
- Healthcare
- Education
- Personal Care
- Subscriptions
- Debt Repayment
- Other

#### 6.4.4 Business Rules

- Expense must have positive amount
- Expense decreases the balance of source account
- System warns if account would go negative but allows transaction
- Category is required

---

### 6.5 Custom Categories

#### 6.5.1 Description
Users can create custom categories beyond the defaults.

#### 6.5.2 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| CAT-01 | User can create custom expense category | Must Have |
| CAT-02 | User can create custom income category | Must Have |
| CAT-03 | User can edit custom category name | Should Have |
| CAT-04 | User can archive custom category | Should Have |
| CAT-05 | Default categories cannot be edited or deleted | Must Have |
| CAT-06 | User can set icon for custom category | Could Have |

---

### 6.6 Recurring Payments

#### 6.6.1 Description
Users can set up recurring payment reminders that appear as pending items on due dates.

#### 6.6.2 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| REC-01 | User can create recurring payment with name, amount, account, category, frequency, due day | Must Have |
| REC-02 | Supported frequencies: Weekly, Monthly, Yearly | Must Have |
| REC-03 | On due date, recurring payment appears in "Pending Payments" | Must Have |
| REC-04 | User can "Mark as Paid" which creates expense from template | Must Have |
| REC-05 | User can adjust amount when marking as paid | Must Have |
| REC-06 | After marking paid, next due date auto-calculates | Must Have |
| REC-07 | Overdue payments show days overdue | Must Have |
| REC-08 | User can skip a payment (moves to next cycle without creating expense) | Should Have |
| REC-09 | User can edit recurring payment template | Must Have |
| REC-10 | User can pause/deactivate recurring payment | Must Have |
| REC-11 | User can delete recurring payment | Must Have |
| REC-12 | User can view history of paid instances | Should Have |

#### 6.6.3 Business Rules

- Recurring payment is a template, not an automatic transaction
- User must explicitly mark as paid to create the expense
- Missed payments stack up as overdue (don't auto-skip)
- Due day for monthly = day of month (1-28, handle edge cases)
- Due day for weekly = day of week (Monday-Sunday)
- Due day for yearly = specific date (month + day)

#### 6.6.4 Example Flow

```
1. User creates: "Gym Membership", 5000 PKR, from Bank, Monthly, Due: 5th
2. On 5th of each month, appears in Pending Payments
3. User taps "Mark as Paid"
4. Optional: adjust amount if different this month
5. Expense created: 5000 from Bank, Category: Subscriptions
6. Next due: 5th of next month
```

---

### 6.7 Loans & Debts

#### 6.7.1 Description
Users can track money they owe to others and money others owe to them, with partial payment support.

#### 6.7.2 Requirements

**Debt Management:**

| ID | Requirement | Priority |
|----|-------------|----------|
| DEB-01 | User can record "I Owe" debt (person, amount, reason, date) | Must Have |
| DEB-02 | User can record "Owe Me" debt (person, amount, reason, date) | Must Have |
| DEB-03 | User can view all active debts in two tabs | Must Have |
| DEB-04 | User can record partial payment against a debt | Must Have |
| DEB-05 | Debt shows original amount and remaining amount | Must Have |
| DEB-06 | Debt auto-marks as settled when remaining = 0 | Must Have |
| DEB-07 | User can view payment history for a debt | Must Have |
| DEB-08 | User can edit debt details | Must Have |
| DEB-09 | User can manually mark debt as settled | Should Have |
| DEB-10 | User can delete debt (with confirmation) | Must Have |

**Linking to Accounts:**

| ID | Requirement | Priority |
|----|-------------|----------|
| DEB-11 | When paying "I Owe" debt, option to create expense from account | Should Have |
| DEB-12 | When receiving "Owe Me" payment, option to create income to account | Should Have |
| DEB-13 | Linked transactions show reference to debt | Should Have |

#### 6.7.3 Business Rules

- Debt payment cannot exceed remaining amount
- Settled debts move to "Settled" section (viewable but separate)
- Linking to account is optional (user might pay outside tracked accounts)
- Person name is free text (no contact integration in MVP)

#### 6.7.4 Example Flow: I Owe

```
1. User records: "Borrowed 5000 from Ahmed for rent"
2. Later, user taps "Record Payment" → enters 2000
3. Option: "Create expense from Bank?" → Yes
4. Expense created: 2000 from Bank, Category: Debt Repayment, Note: "Repayment to Ahmed"
5. Debt shows: Original: 5000, Remaining: 3000
6. User pays remaining 3000
7. Debt marked as Settled
```

---

### 6.8 Reports & Dashboard

#### 6.8.1 Dashboard (Home Screen)

| ID | Requirement | Priority |
|----|-------------|----------|
| DSH-01 | Show all account balances (Real first, then Budget) | Must Have |
| DSH-02 | Show total Real balance (sum of all Real accounts) | Must Have |
| DSH-03 | Show pending recurring payments (due/overdue) | Must Have |
| DSH-04 | Show recent transactions (last 5-10) | Must Have |
| DSH-05 | Quick action buttons: + Income, + Expense, + Transfer | Must Have |
| DSH-06 | Visual indicator for negative balance accounts | Must Have |
| DSH-07 | Visual indicator for overdue payments count | Must Have |

#### 6.8.2 Monthly Report

| ID | Requirement | Priority |
|----|-------------|----------|
| RPT-01 | Show total income for selected month | Must Have |
| RPT-02 | Show total expenses for selected month | Must Have |
| RPT-03 | Show net (income - expenses) for selected month | Must Have |
| RPT-04 | Month selector to navigate between months | Must Have |
| RPT-05 | Expense breakdown by category | Must Have |
| RPT-06 | Category breakdown as pie chart | Must Have |
| RPT-07 | Category breakdown as list with amounts | Must Have |
| RPT-08 | Tap category to see transactions in that category | Should Have |

#### 6.8.3 Filters

| ID | Requirement | Priority |
|----|-------------|----------|
| FLT-01 | Filter transactions by account | Should Have |
| FLT-02 | Filter transactions by category | Should Have |
| FLT-03 | Filter transactions by date range | Could Have |
| FLT-04 | Filter transactions by type (income/expense/transfer) | Should Have |

#### 6.8.4 Transaction History

| ID | Requirement | Priority |
|----|-------------|----------|
| TXN-01 | View all transactions in reverse chronological order | Must Have |
| TXN-02 | Group transactions by date | Must Have |
| TXN-03 | Show transaction type icon (income/expense/transfer) | Must Have |
| TXN-04 | Tap transaction to view details | Must Have |
| TXN-05 | Swipe or tap to edit/delete transaction | Must Have |

---

### 6.9 Daily Reminder

#### 6.9.1 Description
Notification to remind user to log daily expenses.

#### 6.9.2 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| REM-01 | Send daily push notification at user-configured time | Must Have |
| REM-02 | Default time: 8:00 PM | Must Have |
| REM-03 | User can change reminder time | Must Have |
| REM-04 | User can disable reminder | Must Have |
| REM-05 | Tapping notification opens app | Must Have |
| REM-06 | Notification text: "Don't forget to log today's expenses!" | Must Have |

---

### 6.10 Settings

#### 6.10.1 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| SET-01 | Configure daily reminder time | Must Have |
| SET-02 | Enable/disable daily reminder | Must Have |
| SET-03 | Manage custom categories | Must Have |
| SET-04 | View archived accounts | Must Have |
| SET-05 | Set default currency symbol | Should Have |
| SET-06 | Export data (CSV) | Could Have |
| SET-07 | Backup data to file | Could Have |
| SET-08 | Restore data from file | Could Have |
| SET-09 | Reset all data (with confirmation) | Should Have |

---

## 7. Data Model

### 7.1 Entity Relationship Diagram

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   Account    │       │   Transaction    │       │   Category   │
├──────────────┤       ├──────────────────┤       ├──────────────┤
│ id           │◄──────│ from_account_id  │       │ id           │
│ name         │◄──────│ to_account_id    │───────►│ name         │
│ type         │       │ category_id      │───────►│ type         │
│ balance      │       │ type             │       │ is_custom    │
│ is_archived  │       │ amount           │       │ is_archived  │
│ created_at   │       │ date             │       └──────────────┘
│ updated_at   │       │ note             │
└──────────────┘       │ created_at       │
                       └──────────────────┘
                       
┌──────────────────┐       ┌──────────────────┐
│ RecurringPayment │       │   DebtPayment    │
├──────────────────┤       ├──────────────────┤
│ id               │       │ id               │
│ name             │       │ debt_id          │───────┐
│ amount           │       │ amount           │       │
│ account_id       │───────►│ date             │       │
│ category_id      │───────►│ transaction_id   │       │
│ frequency        │       │ created_at       │       │
│ due_day          │       └──────────────────┘       │
│ next_due_date    │                                  │
│ is_active        │       ┌──────────────────┐       │
│ created_at       │       │      Debt        │◄──────┘
└──────────────────┘       ├──────────────────┤
                           │ id               │
                           │ type             │
                           │ person_name      │
                           │ original_amount  │
                           │ remaining_amount │
                           │ reason           │
                           │ date             │
                           │ is_settled       │
                           │ created_at       │
                           └──────────────────┘
```

### 7.2 Schema Definitions

#### Account
```
Account {
  id: UUID (Primary Key)
  name: String (Required, Unique, Max 50 chars)
  type: Enum ['real', 'budget'] (Required)
  balance: Decimal (Required, Default 0)
  is_archived: Boolean (Default false)
  display_order: Integer (Default 0)
  created_at: Timestamp
  updated_at: Timestamp
}
```

#### Transaction
```
Transaction {
  id: UUID (Primary Key)
  type: Enum ['income', 'expense', 'transfer'] (Required)
  amount: Decimal (Required, Positive)
  from_account_id: UUID (Foreign Key → Account, Nullable for income)
  to_account_id: UUID (Foreign Key → Account, Nullable for expense)
  category_id: UUID (Foreign Key → Category, Nullable for transfer)
  date: Date (Required)
  note: String (Max 200 chars, Nullable)
  created_at: Timestamp
  updated_at: Timestamp
}
```

#### Category
```
Category {
  id: UUID (Primary Key)
  name: String (Required, Max 30 chars)
  type: Enum ['income', 'expense'] (Required)
  is_custom: Boolean (Default false)
  is_archived: Boolean (Default false)
  display_order: Integer (Default 0)
  created_at: Timestamp
}
```

#### RecurringPayment
```
RecurringPayment {
  id: UUID (Primary Key)
  name: String (Required, Max 50 chars)
  amount: Decimal (Required, Positive)
  account_id: UUID (Foreign Key → Account, Required)
  category_id: UUID (Foreign Key → Category, Required)
  frequency: Enum ['weekly', 'monthly', 'yearly'] (Required)
  due_day: Integer (Required)
    - weekly: 1-7 (Monday-Sunday)
    - monthly: 1-28
    - yearly: stored as MMDD (e.g., 1225 for Dec 25)
  next_due_date: Date (Required)
  is_active: Boolean (Default true)
  created_at: Timestamp
  updated_at: Timestamp
}
```

#### Debt
```
Debt {
  id: UUID (Primary Key)
  type: Enum ['i_owe', 'owe_me'] (Required)
  person_name: String (Required, Max 50 chars)
  original_amount: Decimal (Required, Positive)
  remaining_amount: Decimal (Required, >= 0)
  reason: String (Max 100 chars, Nullable)
  date: Date (Required)
  is_settled: Boolean (Default false)
  created_at: Timestamp
  updated_at: Timestamp
}
```

#### DebtPayment
```
DebtPayment {
  id: UUID (Primary Key)
  debt_id: UUID (Foreign Key → Debt, Required)
  amount: Decimal (Required, Positive)
  date: Date (Required)
  transaction_id: UUID (Foreign Key → Transaction, Nullable)
  created_at: Timestamp
}
```

### 7.3 Indexes

```
- Account: (is_archived, display_order)
- Transaction: (date DESC), (from_account_id), (to_account_id), (category_id)
- Category: (type, is_archived)
- RecurringPayment: (is_active, next_due_date)
- Debt: (type, is_settled)
- DebtPayment: (debt_id)
```

---

## 8. User Flows

### 8.1 First Launch / Onboarding

```
┌─────────────────────────────────────────────────────────────┐
│                      WELCOME SCREEN                          │
│                                                              │
│  "Welcome to FlowFin"                                        │
│  "Track your money the way it actually flows"                │
│                                                              │
│                    [Get Started]                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SET UP ACCOUNTS                           │
│                                                              │
│  "Let's set up your accounts"                                │
│                                                              │
│  Real Accounts (where your money lives):                     │
│  ┌─────────────────────────────────────────┐                │
│  │ ☑ Bank Account    Starting: [________] │                │
│  │ ☑ Cash/Wallet     Starting: [________] │                │
│  │ ☐ Add another...                        │                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  Budget Accounts (spending allocations):                     │
│  ┌─────────────────────────────────────────┐                │
│  │ ☑ Food Budget                           │                │
│  │ ☑ Transport Budget                      │                │
│  │ ☐ Entertainment Budget                  │                │
│  │ ☐ Add custom...                         │                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│              [Skip]              [Continue]                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SET UP REMINDER                           │
│                                                              │
│  "When should we remind you to log expenses?"                │
│                                                              │
│              ┌────────────────────┐                          │
│              │      8:00 PM       │                          │
│              └────────────────────┘                          │
│                                                              │
│              ☑ Enable daily reminder                         │
│                                                              │
│              [Skip]              [Finish]                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                        HOME SCREEN
```

### 8.2 Adding an Expense (Primary Flow)

```
HOME SCREEN
     │
     │ Tap [+ Expense]
     ▼
┌─────────────────────────────────────────────────────────────┐
│                      ADD EXPENSE                             │
│                                                              │
│  Amount                                                      │
│  ┌─────────────────────────────────────────┐                │
│  │ Rs.                             500     │                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  From Account                                                │
│  ┌─────────────────────────────────────────┐                │
│  │ Food Budget                    (9,500) ▼│                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  Category                                                    │
│  ┌─────────────────────────────────────────┐                │
│  │ Food & Dining                         ▼│                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  Date                                                        │
│  ┌─────────────────────────────────────────┐                │
│  │ Today, Dec 27, 2025                   ▼│                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  Note (optional)                                             │
│  ┌─────────────────────────────────────────┐                │
│  │ Mess lunch                              │                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│                       [Save]                                 │
└─────────────────────────────────────────────────────────────┘
     │
     │ Save
     ▼
HOME SCREEN (Food Budget now shows 9,000)
```

### 8.3 ATM Withdrawal (Transfer Flow)

```
HOME SCREEN
     │
     │ Tap [+ Transfer] or [Bank Account] → [Transfer Out]
     ▼
┌─────────────────────────────────────────────────────────────┐
│                      TRANSFER                                │
│                                                              │
│  Amount                                                      │
│  ┌─────────────────────────────────────────┐                │
│  │ Rs.                           5,000     │                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  From                                                        │
│  ┌─────────────────────────────────────────┐                │
│  │ Bank Account                 (45,000) ▼│                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  To                                                          │
│  ┌─────────────────────────────────────────┐                │
│  │ Cash                          (2,000) ▼│                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  Date                                                        │
│  ┌─────────────────────────────────────────┐                │
│  │ Today, Dec 27, 2025                   ▼│                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  Note (optional)                                             │
│  ┌─────────────────────────────────────────┐                │
│  │ ATM withdrawal                          │                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│                       [Save]                                 │
└─────────────────────────────────────────────────────────────┘
     │
     │ Save
     ▼
HOME SCREEN (Bank: 40,000 | Cash: 7,000)
```

### 8.4 Monthly Budget Allocation

```
User gets salary → Adds income → Allocates to budgets

1. ADD INCOME
   Amount: 150,000
   To: Bank Account
   Category: Salary
   
   → Bank Account: 150,000

2. TRANSFER to Food Budget
   Amount: 15,000
   From: Bank Account
   To: Food Budget
   
   → Bank: 135,000 | Food Budget: 15,000

3. TRANSFER to Transport Budget
   Amount: 5,000
   From: Bank Account
   To: Transport Budget
   
   → Bank: 130,000 | Transport: 5,000

4. TRANSFER to Entertainment Budget
   Amount: 3,000
   From: Bank Account
   To: Entertainment Budget
   
   → Bank: 127,000 | Entertainment: 3,000

Result: User has allocated budgets, remaining in bank for bills/savings
```

### 8.5 Recurring Payment Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    HOME - PENDING PAYMENTS                   │
│                                                              │
│  ⚠️ 2 Pending Payments                                       │
│  ┌─────────────────────────────────────────┐                │
│  │ 🔴 Gym Membership                       │                │
│  │    Rs. 5,000 from Bank                  │                │
│  │    Due: Today                           │                │
│  │                        [Mark as Paid]   │                │
│  ├─────────────────────────────────────────┤                │
│  │ 🟡 Netflix                              │                │
│  │    Rs. 1,500 from Bank                  │                │
│  │    Due: 3 days overdue                  │                │
│  │                        [Mark as Paid]   │                │
│  └─────────────────────────────────────────┘                │
└─────────────────────────────────────────────────────────────┘
     │
     │ Tap [Mark as Paid] on Gym
     ▼
┌─────────────────────────────────────────────────────────────┐
│                    CONFIRM PAYMENT                           │
│                                                              │
│  Gym Membership                                              │
│                                                              │
│  Amount                                                      │
│  ┌─────────────────────────────────────────┐                │
│  │ Rs.                           5,000     │  (editable)    │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  From: Bank Account                                          │
│  Category: Subscriptions                                     │
│  Date: Today                                                 │
│                                                              │
│         [Cancel]              [Confirm Payment]              │
└─────────────────────────────────────────────────────────────┘
     │
     │ Confirm
     ▼
- Expense created: 5,000 from Bank
- Next due date: Jan 27, 2026
- Pending list updated
```

### 8.6 Debt Repayment Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    DEBTS - I OWE                             │
│                                                              │
│  ┌─────────────────────────────────────────┐                │
│  │ Ahmed                                   │                │
│  │ "Borrowed for rent"                     │                │
│  │ Original: Rs. 5,000                     │                │
│  │ Remaining: Rs. 5,000                    │                │
│  │ Date: Dec 20, 2025                      │                │
│  │                      [Record Payment]   │                │
│  └─────────────────────────────────────────┘                │
└─────────────────────────────────────────────────────────────┘
     │
     │ Tap [Record Payment]
     ▼
┌─────────────────────────────────────────────────────────────┐
│                    RECORD PAYMENT                            │
│                                                              │
│  Paying: Ahmed                                               │
│  Remaining: Rs. 5,000                                        │
│                                                              │
│  Payment Amount                                              │
│  ┌─────────────────────────────────────────┐                │
│  │ Rs.                           2,000     │                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│  ☑ Create expense from account                              │
│  ┌─────────────────────────────────────────┐                │
│  │ Bank Account                         ▼│                │
│  └─────────────────────────────────────────┘                │
│                                                              │
│         [Cancel]              [Record Payment]               │
└─────────────────────────────────────────────────────────────┘
     │
     │ Record
     ▼
- DebtPayment created: 2,000
- Expense created: 2,000 from Bank, Category: Debt Repayment
- Debt updated: Remaining: 3,000
```

---

## 9. Screen Specifications

### 9.1 Screen Inventory

| Screen | Purpose | Navigation |
|--------|---------|------------|
| Home/Dashboard | Account balances, quick actions, pending payments, recent transactions | Tab bar item 1 |
| Add Income | Form to record income | FAB or quick action |
| Add Expense | Form to record expense | FAB or quick action |
| Transfer | Form to transfer between accounts | FAB or quick action |
| Accounts List | View all accounts | Tab bar item 2 |
| Account Detail | Single account history | Tap account in list |
| Recurring Payments | List and manage recurring | Tab bar item 3 |
| Add/Edit Recurring | Form for recurring template | From recurring list |
| Debts | I Owe / Owe Me tabs | Tab bar item 4 |
| Add Debt | Form to create debt | From debts screen |
| Debt Detail | View debt and payments | Tap debt in list |
| Record Debt Payment | Form to record payment | From debt detail |
| Reports | Monthly summary and charts | Tab bar item 5 |
| Settings | App configuration | Profile/Menu icon |
| Categories Manager | Manage custom categories | From settings |

### 9.2 Navigation Structure

```
┌─────────────────────────────────────────────────────────────┐
│                                                              │
│                      [Current Screen]                        │
│                                                              │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│      🏠          💳          🔄          🤝          📊     │
│     Home      Accounts   Recurring    Debts      Reports    │
│                                                              │
└─────────────────────────────────────────────────────────────┘

Floating Action Button (FAB) on Home:
  → + Income
  → + Expense  
  → + Transfer
```

### 9.3 Home Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│ FlowFin                                          ⚙️ Settings │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  TOTAL BALANCE                                               │
│  Rs. 47,000                                                  │
│  ───────────────────────────────────────────────────────    │
│                                                              │
│  ACCOUNTS                                    [See All →]     │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐   │
│  │ 🏦 Bank        │ │ 💵 Cash        │ │ 🍕 Food        │   │
│  │ Rs. 40,000    │ │ Rs. 7,000     │ │ Rs. 9,000     │   │
│  └────────────────┘ └────────────────┘ └────────────────┘   │
│  ┌────────────────┐ ┌────────────────┐                      │
│  │ 🚗 Transport   │ │ 🎮 Fun         │                      │
│  │ Rs. 4,500     │ │ Rs. 2,500     │                      │
│  └────────────────┘ └────────────────┘                      │
│                                                              │
│  ───────────────────────────────────────────────────────    │
│                                                              │
│  ⚠️ PENDING PAYMENTS (2)                     [See All →]     │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ 🔴 Gym          Rs. 5,000    Due today  [Pay]      │    │
│  │ 🟡 Netflix      Rs. 1,500    3 days ago [Pay]      │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ───────────────────────────────────────────────────────    │
│                                                              │
│  RECENT TRANSACTIONS                         [See All →]     │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ ↓ Mess              Food Budget     -500   Today   │    │
│  │ ↔ ATM Withdrawal    Bank → Cash   -5,000   Today   │    │
│  │ ↓ Rickshaw          Cash           -100   Today   │    │
│  │ ↑ Salary            Bank       +150,000   Dec 25   │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│                         [+]                                  │
│      🏠          💳          🔄          🤝          📊     │
└─────────────────────────────────────────────────────────────┘
```

### 9.4 Add Expense Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│ ←  Add Expense                                               │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│                                                              │
│                         Rs.                                  │
│                        ┌───────────────┐                     │
│                        │     500       │                     │
│                        └───────────────┘                     │
│                                                              │
│  ───────────────────────────────────────────────────────    │
│                                                              │
│  From Account                                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  🍕 Food Budget                         Rs. 9,500 > │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  Category                                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  🍽️ Food & Dining                                  > │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  Date                                                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  📅 Today, Dec 27, 2025                            > │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  Note                                                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Mess lunch                                          │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    Save Expense                      │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 9.5 Reports Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│ Reports                                          ⚙️ Settings │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│           ◀  December 2025  ▶                               │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  INCOME           EXPENSES           NET            │    │
│  │  Rs. 150,000      Rs. 45,000       +Rs. 105,000    │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ───────────────────────────────────────────────────────    │
│                                                              │
│  EXPENSES BY CATEGORY                                        │
│                                                              │
│              ┌──────────────┐                                │
│          ╱───│              │───╲                            │
│        ╱     │   [PIE       │     ╲                          │
│       │      │   CHART]     │      │                         │
│        ╲     │              │     ╱                          │
│          ╲───│              │───╱                            │
│              └──────────────┘                                │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ 🍕 Food & Dining                      Rs. 15,000   │    │
│  │ ████████████████░░░░░░░░░░░░░░░          33%      │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │ 🏠 Rent                               Rs. 12,000   │    │
│  │ █████████████░░░░░░░░░░░░░░░░░░          27%      │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │ 🚗 Transport                           Rs. 5,000   │    │
│  │ █████░░░░░░░░░░░░░░░░░░░░░░░░░░          11%      │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │ ⚡ Utilities                           Rs. 4,500   │    │
│  │ █████░░░░░░░░░░░░░░░░░░░░░░░░░░          10%      │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │ 📦 Other                               Rs. 8,500   │    │
│  │ ████████░░░░░░░░░░░░░░░░░░░░░░░          19%      │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│      🏠          💳          🔄          🤝          📊     │
└─────────────────────────────────────────────────────────────┘
```

---

## 10. Technical Requirements

### 10.1 Platform Requirements

| Requirement | Specification |
|-------------|---------------|
| Primary Platform | Mobile (iOS and Android) |
| Secondary Platform | Web (future consideration) |
| Minimum iOS Version | iOS 14.0+ |
| Minimum Android Version | Android 8.0 (API 26)+ |
| Offline Support | Full functionality without internet |
| Data Storage | Local (SQLite/Realm) |

### 10.2 Technology Recommendations

**Option A: React Native**
- Pros: Single codebase, large ecosystem, familiar for web developers
- Cons: Performance overhead, native module complexity

**Option B: Flutter**
- Pros: Excellent performance, beautiful UI, growing ecosystem
- Cons: Dart learning curve, larger app size

**Recommendation:** Flutter — better suited for financial app UI requirements and offline-first architecture.

### 10.3 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Screens   │ │   Widgets   │ │    State    │           │
│  │             │ │             │ │  Management │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│                       DOMAIN LAYER                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Entities  │ │  Use Cases  │ │ Repositories│           │
│  │             │ │             │ │ (Abstract)  │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│                        DATA LAYER                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Models    │ │ Repositories│ │  Local DB   │           │
│  │             │ │   (Impl)    │ │  (SQLite)   │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### 10.4 State Management

Recommended: **Riverpod** (Flutter) or **Redux Toolkit** (React Native)

Key state:
- Accounts list with balances
- Current month transactions
- Pending recurring payments
- Active debts
- UI state (selected filters, loading states)

### 10.5 Local Database Schema

Use SQLite with migrations support.

```sql
-- Accounts Table
CREATE TABLE accounts (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  type TEXT NOT NULL CHECK (type IN ('real', 'budget')),
  balance REAL NOT NULL DEFAULT 0,
  is_archived INTEGER NOT NULL DEFAULT 0,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

-- Categories Table
CREATE TABLE categories (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  type TEXT NOT NULL CHECK (type IN ('income', 'expense')),
  is_custom INTEGER NOT NULL DEFAULT 0,
  is_archived INTEGER NOT NULL DEFAULT 0,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL
);

-- Transactions Table
CREATE TABLE transactions (
  id TEXT PRIMARY KEY,
  type TEXT NOT NULL CHECK (type IN ('income', 'expense', 'transfer')),
  amount REAL NOT NULL,
  from_account_id TEXT REFERENCES accounts(id),
  to_account_id TEXT REFERENCES accounts(id),
  category_id TEXT REFERENCES categories(id),
  date TEXT NOT NULL,
  note TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

-- Recurring Payments Table
CREATE TABLE recurring_payments (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  amount REAL NOT NULL,
  account_id TEXT NOT NULL REFERENCES accounts(id),
  category_id TEXT NOT NULL REFERENCES categories(id),
  frequency TEXT NOT NULL CHECK (frequency IN ('weekly', 'monthly', 'yearly')),
  due_day INTEGER NOT NULL,
  next_due_date TEXT NOT NULL,
  is_active INTEGER NOT NULL DEFAULT 1,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

-- Debts Table
CREATE TABLE debts (
  id TEXT PRIMARY KEY,
  type TEXT NOT NULL CHECK (type IN ('i_owe', 'owe_me')),
  person_name TEXT NOT NULL,
  original_amount REAL NOT NULL,
  remaining_amount REAL NOT NULL,
  reason TEXT,
  date TEXT NOT NULL,
  is_settled INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

-- Debt Payments Table
CREATE TABLE debt_payments (
  id TEXT PRIMARY KEY,
  debt_id TEXT NOT NULL REFERENCES debts(id),
  amount REAL NOT NULL,
  date TEXT NOT NULL,
  transaction_id TEXT REFERENCES transactions(id),
  created_at TEXT NOT NULL
);

-- Indexes
CREATE INDEX idx_transactions_date ON transactions(date DESC);
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_recurring_active_due ON recurring_payments(is_active, next_due_date);
CREATE INDEX idx_debts_type_settled ON debts(type, is_settled);
```

### 10.6 Performance Requirements

| Metric | Target |
|--------|--------|
| App launch time | < 2 seconds |
| Screen transition | < 300ms |
| Transaction save | < 500ms |
| Report generation | < 1 second |
| Database query | < 100ms |

### 10.7 Security Requirements

| Requirement | Implementation |
|-------------|----------------|
| Data at rest | SQLite encryption (SQLCipher) |
| No cloud sync (MVP) | All data local |
| Backup files | Encrypted export |
| App lock (post-MVP) | Biometric/PIN |

---

## 11. Success Metrics

### 11.1 Acquisition Metrics

| Metric | Target (6 months) |
|--------|-------------------|
| Downloads | 10,000 |
| Organic installs | 60% |
| App Store rating | 4.5+ |

### 11.2 Engagement Metrics

| Metric | Target |
|--------|--------|
| Daily Active Users (DAU) | 30% of installs |
| Weekly Active Users (WAU) | 50% of installs |
| Transactions per active user per week | 10+ |
| Retention D7 | 40% |
| Retention D30 | 25% |

### 11.3 Feature Usage Metrics

| Feature | Target Usage |
|---------|--------------|
| Accounts created per user | 4+ |
| Expenses logged per month | 30+ |
| Transfers per month | 5+ |
| Recurring payments set up | 2+ |
| Debts tracked | 1+ |
| Reports viewed per month | 4+ |

### 11.4 Quality Metrics

| Metric | Target |
|--------|--------|
| Crash-free sessions | 99.5% |
| App Store reviews responded | 100% |
| Critical bugs | 0 |
| Average bug fix time | < 48 hours |

---

## 12. Release Plan

### 12.1 MVP Phases

**Phase 1: Core Foundation (Weeks 1-3)**
- Database setup and migrations
- Account management (CRUD)
- Basic navigation structure
- Home screen with account balances

**Phase 2: Transactions (Weeks 4-6)**
- Income entry
- Expense entry
- Transfer entry
- Transaction history
- Category management

**Phase 3: Advanced Features (Weeks 7-9)**
- Recurring payments with pending workflow
- Debt tracking with partial payments
- Account linking for debts

**Phase 4: Reports & Polish (Weeks 10-11)**
- Monthly reports
- Category breakdown charts
- Daily reminder notifications
- Settings screen
- Onboarding flow

**Phase 5: Testing & Launch (Week 12)**
- Beta testing
- Bug fixes
- Performance optimization
- App store submission

### 12.2 Release Checklist

**Pre-Launch:**
- [ ] All MVP features complete
- [ ] 50+ beta testers feedback incorporated
- [ ] Performance benchmarks met
- [ ] Security review complete
- [ ] App store assets ready (screenshots, description)
- [ ] Privacy policy published

**Launch:**
- [ ] iOS App Store submission
- [ ] Google Play Store submission
- [ ] Landing page live
- [ ] Social media announcement

**Post-Launch:**
- [ ] Monitor crash reports
- [ ] Respond to reviews
- [ ] Track success metrics
- [ ] Prioritize v1.1 features

---

## 13. AI Features Strategy

### 13.1 Philosophy

> **"AI assists, user confirms. Data stays on device."**

All AI features should:
- Run locally on-device (no cloud API calls)
- Enhance speed, not replace user control
- Learn from user's own patterns
- Be transparent about suggestions
- Allow easy correction when wrong

### 13.2 Planned AI Features

#### Phase 1: Smart Suggestions (v1.2)

**Category Prediction**
| Signal | Usage |
|--------|-------|
| Amount patterns | "500" from Food Budget → likely "Mess" |
| Time of day | Morning expense → "Breakfast/Chai" |
| Day of week | Weekend entertainment patterns |
| Account selected | Food Budget → food categories ranked first |
| Recent history | Last 5 categories used shown first |

```
User enters: 500 from Food Budget
AI suggests: 🍽️ Mess (95%) | 🛒 Groceries (3%) | ☕ Chai (2%)
User taps to confirm or selects different
```

**Smart Account Selection**
- Remember which account is used most at certain times
- Suggest based on amount (small amounts → Cash, large → Bank)

**Auto-Note Generation**
| Context | Generated Note |
|---------|----------------|
| Recurring-like amount | "Weekly mess" (if 500 from Food every week) |
| Sequential transactions | "Grocery trip" (multiple expenses same day) |
| Time-based | "Morning chai" (small expense, early morning) |

#### Phase 2: Intelligent Assistance (v2.0)

**Voice-to-Transaction (Enhanced)**
```
User says: "Paid Ahmed 2000 from bank"
AI parses:
  → Type: Could be Expense OR Debt Payment
  → Shows: "Is this a debt repayment to Ahmed or a regular expense?"
  → If debt exists for Ahmed, pre-selects debt payment flow
```

**Transaction Splitting**
```
User says: "Withdrew 5000 and bought groceries for 2000"
AI creates:
  1. Transfer: Bank → Cash (5,000)
  2. Expense: Cash → Groceries (2,000)
```

**Anomaly Alerts**
- "You usually spend ~8000 on food by this date. You've spent 12,000."
- "Unusual: 15,000 expense from Entertainment (your average is 3,000)"

#### Phase 3: Predictive Features (v3.0)

**Budget Recommendations**
- "Based on 3 months of data, you typically need 12,000 for food, not 10,000"
- "You have 3,000 left in Food but 8 days remaining — pace: 375/day"

**Cash Flow Prediction**
- "Based on your recurring payments, you'll need 25,000 by the 10th"
- "At current spending rate, Transport budget will run out in 4 days"

**Smart Recurring Detection**
- Detect patterns: "You pay ~5000 to 'Gym' every month around the 5th"
- Suggest: "Create recurring payment?"

### 13.3 On-Device ML Implementation

#### Technology Options

| Platform | Technology | Use Case |
|----------|------------|----------|
| Cross-platform | TensorFlow Lite | Category classification, amount patterns |
| Cross-platform | ONNX Runtime | Lightweight inference |
| iOS | Core ML | Native Apple optimization |
| Android | ML Kit | Google's on-device ML |
| Flutter | tflite_flutter | TensorFlow Lite wrapper |

#### Recommended Approach

**For Flutter (MVP):**
```
┌─────────────────────────────────────────────────────────────┐
│                      APP LAYER                               │
├─────────────────────────────────────────────────────────────┤
│                   ML Service Layer                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │  Category   │ │    Note     │ │   Pattern           │   │
│  │  Predictor  │ │  Generator  │ │   Detector          │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                   TensorFlow Lite                            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Local Models (bundled + user-trained)              │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

#### Model Training Strategy

**Bundled Models (Ship with app):**
- Generic category predictor trained on common patterns
- Basic note templates
- ~1-2MB model size

**On-Device Learning:**
- Model fine-tunes on user's own transactions
- Learns personal patterns over 2-4 weeks
- No data leaves device

**Federated Learning (Future):**
- Optional: Improve global model without sharing raw data
- User opts in to contribute anonymized patterns

### 13.4 AI Feature Requirements

| ID | Requirement | Priority | Version |
|----|-------------|----------|---------|
| AI-01 | Category suggestion based on account + amount | High | v1.2 |
| AI-02 | Show confidence score for suggestions | Medium | v1.2 |
| AI-03 | Learn from user corrections | High | v1.2 |
| AI-04 | Auto-generate notes for recurring-like transactions | Medium | v1.2 |
| AI-05 | Voice input parsing to transaction fields | High | v1.2 |
| AI-06 | Multi-step voice commands (withdraw + spend) | Medium | v2.0 |
| AI-07 | Spending anomaly detection | Medium | v2.0 |
| AI-08 | Budget pacing alerts | Medium | v2.0 |
| AI-09 | Recurring payment pattern detection | Low | v3.0 |
| AI-10 | Cash flow prediction | Low | v3.0 |

### 13.5 AI UX Principles

1. **Suggestions, Not Automation**
   - AI suggests, user confirms with one tap
   - Never auto-save without user action
   
2. **Graceful Degradation**
   - App works fully without AI features
   - New users get generic suggestions until model learns

3. **Transparent Confidence**
   - Show why AI made a suggestion when possible
   - "Based on your last 5 similar transactions"

4. **Easy Correction**
   - Wrong suggestion? One tap to change
   - Corrections improve future suggestions

5. **Privacy First**
   - All processing on-device
   - No transaction data sent to cloud
   - Clear explanation in onboarding

### 13.6 Example AI-Enhanced Flows

**Quick Expense with AI:**
```
1. User taps [+ Expense]
2. Enters amount: 500
3. Selects account: Food Budget
4. AI instantly suggests:
   ┌─────────────────────────────────────────┐
   │ Category: 🍽️ Mess          [Change]    │
   │ Note: "Daily mess"         [Edit]      │
   │                                         │
   │ Based on: Similar transactions at this  │
   │ time from Food Budget                   │
   └─────────────────────────────────────────┘
5. User taps [Save] — done in 3 seconds
```

**Voice Entry with AI:**
```
1. User taps microphone icon
2. Says: "Spent 200 on rickshaw from cash"
3. AI shows parsed result:
   ┌─────────────────────────────────────────┐
   │ ✓ Amount: Rs. 200                       │
   │ ✓ Account: Cash                         │
   │ ✓ Category: Transport                   │
   │ ? Note: "Rickshaw"                      │
   │                                         │
   │        [Edit]        [Save]             │
   └─────────────────────────────────────────┘
4. User taps [Save] or edits if needed
```

**Anomaly Alert:**
```
┌─────────────────────────────────────────────────────────────┐
│ ⚠️ Spending Alert                                           │
│                                                              │
│ Your Food spending this month: Rs. 18,000                   │
│ Your usual by this date: Rs. 10,000                         │
│                                                              │
│ You're spending 80% faster than normal.                     │
│                                                              │
│ [Dismiss]                    [View Food Transactions]       │
└─────────────────────────────────────────────────────────────┘
```

---

## 14. Future Roadmap

### 14.1 Version 1.1 (Post-MVP)

| Feature | Description | Priority |
|---------|-------------|----------|
| Quick Actions | Customizable shortcuts for frequent transactions | High |
| Batch Entry | Enter multiple transactions at once | High |
| Data Export | CSV export of transactions | High |
| Data Backup | Export/import database file | High |
| Budget Targets | Optional monthly targets for budget accounts | Medium |

### 14.2 Version 1.2

| Feature | Description | Priority |
|---------|-------------|----------|
| Voice Entry | Natural language transaction input | High |
| Receipt Attachments | Photo attachments for expenses | Medium |
| Recurring Income | Auto-reminder for regular income | Medium |
| Widget | Home screen balance widget | Medium |
| Search | Search transactions by note/amount | Medium |

### 14.3 Version 2.0

| Feature | Description | Priority |
|---------|-------------|----------|
| Cloud Sync | Optional cloud backup and sync | High |
| Multi-device | Access from multiple devices | High |
| App Lock | Biometric/PIN security | High |
| Custom Date Range Reports | Flexible report periods | Medium |
| Multi-currency | Handle foreign currency transactions | Medium |

### 14.4 Version 3.0

| Feature | Description | Priority |
|---------|-------------|----------|
| Family/Shared Mode | Shared accounts for couples/families | Medium |
| Insights | AI-powered spending insights | Low |
| Goals | Savings goals with progress tracking | Medium |
| Bill Reminders | Smart reminders from transaction patterns | Low |

---

## 15. Open Questions

### 15.1 Product Decisions

| Question | Options | Recommendation | Status |
|----------|---------|----------------|--------|
| App name | FlowFin, MoneyFlow, CashTrail, etc. | FlowFin | Pending |
| Default currency | PKR, configurable | PKR with option to change | Pending |
| Negative balance handling | Block, warn, allow | Warn but allow | Decided |
| Onboarding depth | Minimal vs guided setup | Guided with skip option | Decided |
| Transaction edit/delete | Allow freely or restrict | Allow with confirmation | Decided |

### 15.2 Technical Decisions

| Question | Options | Recommendation | Status |
|----------|---------|----------------|--------|
| Framework | Flutter vs React Native | Flutter | Pending |
| State management | Riverpod, Bloc, Provider | Riverpod | Pending |
| Database | SQLite, Hive, Realm | SQLite with drift | Pending |
| CI/CD | GitHub Actions, Codemagic | Codemagic | Pending |

### 15.3 Design Decisions

| Question | Options | Recommendation | Status |
|----------|---------|----------------|--------|
| Color scheme | Blue/Green financial, Modern minimal | Modern minimal | Pending |
| Dark mode | MVP or later | Later (v1.1) | Pending |
| Animations | Minimal or expressive | Minimal, functional | Pending |
| Typography | System fonts or custom | System fonts | Pending |

---

## 16. Appendix

### 16.1 Glossary

| Term | Definition |
|------|------------|
| Real Account | An account representing actual money location (bank, cash, wallet) |
| Budget Account | An account representing allocated funds for a purpose |
| Transfer | Moving money between two accounts |
| Recurring Payment | A template for regular expenses that generates pending items |
| Pending Payment | A recurring payment that is due but not yet marked as paid |
| I Owe | Money the user has borrowed from someone |
| Owe Me | Money someone has borrowed from the user |

### 16.2 Competitor Analysis Summary

| App | Strengths | Weaknesses | Price |
|-----|-----------|------------|-------|
| YNAB | Proven methodology, strong community | Complex, expensive, bank sync issues | $109/year |
| Goodbudget | Simple envelope system | Limited features, dated UI | Free/$80/year |
| Monarch | Beautiful UI, good for couples | Expensive, requires bank sync | $99/year |
| PocketGuard | Simple "safe to spend" | Limited budgeting control | Free/$75/year |

### 16.3 User Research Summary

Key insights from initial user interviews:
1. Manual entry is acceptable if it's fast (< 10 seconds)
2. Debt tracking with friends is a major unmet need
3. Cash transactions are very common (50%+ of expenses)
4. Existing apps feel "too American" (bank sync focus)
5. Monthly budget allocation is the desired workflow

### 16.4 References

- YNAB Methodology: https://www.ynab.com/the-four-rules
- Envelope Budgeting: https://en.wikipedia.org/wiki/Envelope_system
- Zero-based Budgeting: https://en.wikipedia.org/wiki/Zero-based_budgeting

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | Dec 27, 2025 | — | Initial draft |

---

*End of Document*
