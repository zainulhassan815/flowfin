package com.flowfin.lock

import android.content.Context
import android.os.SystemClock
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Biometric *or* the device's PIN / pattern / password. The app holds no secret
 *  of its own: no hashing, no rate limiting, no lockout policy, no reset flow —
 *  all of which the platform already does, and better. WEAK rather than STRONG
 *  because nothing here unwraps a key; STRONG would only narrow the hardware. */
private const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

/**
 * How long the app may sit in the background before it locks again. Without a
 * grace window every system dialog that steals focus — and every rotation —
 * would re-prompt.
 */
private const val GRACE_MS = 30_000L

/**
 * Whether the ledger is currently behind the lock, for the life of the process.
 *
 * Process-scoped rather than remembered in composition, so an Activity recreated
 * by a rotation doesn't re-prompt, and a genuinely new process always starts
 * locked.
 */
class AppLock(
  /** Elapsed realtime, injected so the grace window is testable without a device. */
  private val elapsedMillis: () -> Long = SystemClock::elapsedRealtime,
) {

  var unlocked by mutableStateOf(false)
    private set

  private var backgroundedAt = 0L

  fun unlock() {
    unlocked = true
  }

  fun onBackground() {
    backgroundedAt = elapsedMillis()
  }

  /**
   * Re-lock if the app was away long enough. Measured on elapsed realtime rather
   * than the wall clock, so moving the device clock forward can't extend the
   * window.
   */
  fun onForeground() {
    if (unlocked && elapsedMillis() - backgroundedAt > GRACE_MS) {
      unlocked = false
    }
  }
}

/**
 * Whether this device can authenticate at all.
 *
 * Consulted both by the Settings row, which won't offer a lock the device can't
 * open, and by the gate itself — PRD LOCK-06. A user who removes their screen
 * lock after switching this on would otherwise be left with a ledger nobody can
 * reach, so the gate stands aside rather than standing in the way.
 */
fun canAuthenticate(context: Context): Boolean =
  BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Ask the platform to authenticate, calling [onUnlocked] only on success.
 *
 * Failure and cancellation are deliberately silent: the prompt has already said
 * what went wrong, and the lock screen behind it is the retry.
 */
fun promptToUnlock(
  activity: FragmentActivity,
  title: String,
  subtitle: String,
  onUnlocked: () -> Unit,
) {
  val prompt = BiometricPrompt(
    activity,
    ContextCompat.getMainExecutor(activity),
    object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onUnlocked()
    },
  )
  prompt.authenticate(
    BiometricPrompt.PromptInfo.Builder()
      .setTitle(title)
      .setSubtitle(subtitle)
      // No negative button: allowing DEVICE_CREDENTIAL supplies its own, and
      // setting both throws.
      .setAllowedAuthenticators(AUTHENTICATORS)
      .build(),
  )
}
