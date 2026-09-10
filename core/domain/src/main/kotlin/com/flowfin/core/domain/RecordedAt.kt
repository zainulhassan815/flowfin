package com.flowfin.core.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * The instant to stamp on a ledger row the user dated [this].
 *
 * **Today gets the current time.** The forms only ask for a date, so a row dated
 * today would otherwise carry the same stamp as every other row entered today and
 * sort below anything holding a real clock time — a schedule that fired this
 * morning, say — however recently it was typed. Every feed in the app orders by
 * this value, so "just added" has to sort like it.
 *
 * **Any other date gets noon**, not midnight: a back-dated row has no time of day
 * to claim, and noon keeps it inside its own day under any timezone the ledger is
 * later read in.
 */
fun LocalDate.recordedAt(clock: Clock, zone: TimeZone): Instant {
  val now = clock.now()
  return if (this == now.toLocalDateTime(zone).date) now else atTime(12, 0).toInstant(zone)
}
