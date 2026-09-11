package com.flowfin.lock

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLockTest {

  private var elapsed = 1_000L
  private val lock = AppLock { elapsed }

  @Test
  fun `a fresh process starts locked`() {
    assertFalse(lock.unlocked)
  }

  @Test
  fun `a short trip away doesn't re-lock`() {
    lock.unlock()
    lock.onBackground()
    elapsed += 5_000
    lock.onForeground()
    assertTrue(lock.unlocked, "a glance at another app shouldn't demand the PIN again")
  }

  @Test
  fun `a long absence re-locks`() {
    lock.unlock()
    lock.onBackground()
    elapsed += 60_000
    lock.onForeground()
    assertFalse(lock.unlocked)
  }

  @Test
  fun `coming back never unlocks something that was locked`() {
    lock.onBackground()
    elapsed += 1_000
    lock.onForeground()
    assertFalse(lock.unlocked)
  }
}
