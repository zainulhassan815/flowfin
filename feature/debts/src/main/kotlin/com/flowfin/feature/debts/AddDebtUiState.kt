package com.flowfin.feature.debts

import com.flowfin.core.model.AccountId
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.Money
import com.flowfin.core.model.PersonId
import com.flowfin.core.ui.CalculatorState
import com.flowfin.core.ui.UiText

/** Which picker sheet is open. */
enum class AddDebtSheet { Amount, Person, Account, Reason }

/** A contact the debt can be opened against. */
data class DebtPersonOption(val id: PersonId, val name: String, val avatarTintIndex: Int)

/** A real, active account the debt's money can move through. */
data class DebtAccountOption(
  val id: AccountId,
  val name: String,
  val iconKey: String?,
  val colorKey: String?,
  val balance: String,
)

/**
 * The Add-Debt form. [direction] flips every label on the screen — borrowing
 * puts money *into* an account, lending takes it *out* — and decides which use
 * case runs on save.
 *
 * The account link is optional ([linkAccount]): off, the debt is tracked
 * off-book with no transaction against any balance. [newPersonName] holds a
 * name typed into the person sheet that doesn't match an existing contact; it's
 * created on save.
 */
data class AddDebtUiState(
  val direction: DebtDirection = DebtDirection.I_OWE,
  val calculator: CalculatorState = CalculatorState(),
  val amountWhole: String = "0",
  val amountDecimal: String? = null,
  val expression: String? = null,
  val amount: Money? = null,
  val person: PersonId? = null,
  val newPersonName: String = "",
  val personQuery: String = "",
  val reason: String = "",
  val dateLabel: UiText = UiText.Raw(""),
  val linkAccount: Boolean = false,
  val account: AccountId? = null,
  val personOptions: List<DebtPersonOption> = emptyList(),
  val accountOptions: List<DebtAccountOption> = emptyList(),
  val openSheet: AddDebtSheet? = null,
  val submitting: Boolean = false,
) {
  val isBorrowing: Boolean get() = direction == DebtDirection.I_OWE

  /** The chosen contact's name, or the new one being typed — whichever is set. */
  val personName: String?
    get() = selectedPerson()?.name ?: newPersonName.trim().ifBlank { null }

  /** Contacts matching what's typed in the person sheet's search field. */
  val filteredPersons: List<DebtPersonOption>
    get() = personQuery.trim().let { q ->
      if (q.isEmpty()) personOptions else personOptions.filter { it.name.contains(q, ignoreCase = true) }
    }

  /** True when the typed name is genuinely new — offer to create it. */
  val canCreatePerson: Boolean
    get() = personQuery.isNotBlank() && personOptions.none { it.name.equals(personQuery.trim(), ignoreCase = true) }

  val canSave: Boolean
    get() = !submitting &&
      amount?.isPositive == true &&
      personName != null &&
      (!linkAccount || account != null)

  fun selectedPerson(): DebtPersonOption? = personOptions.firstOrNull { it.id == person }
  fun selectedAccount(): DebtAccountOption? = accountOptions.firstOrNull { it.id == account }
}
