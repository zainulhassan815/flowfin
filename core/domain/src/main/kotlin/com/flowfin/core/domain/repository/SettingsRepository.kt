package com.flowfin.core.domain.repository

import com.flowfin.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * User preferences, persisted outside the database — they aren't financial
 * records and shouldn't ride along with a data wipe or an export.
 */
interface SettingsRepository {

  fun observe(): Flow<UserSettings>

  /** Apply a change. One method rather than a setter per field: every caller
   *  already holds a [UserSettings], and the file is written whole either way. */
  suspend fun update(transform: (UserSettings) -> UserSettings)
}
