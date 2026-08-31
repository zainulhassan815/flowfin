package com.flowfin.core.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.model.ThemePreference
import com.flowfin.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal class SettingsRepositoryImpl(
  private val store: DataStore<UserSettings>,
) : SettingsRepository {

  // A read failure must not take the UI down with it — settings are a
  // convenience, and defaults are a valid app.
  override fun observe(): Flow<UserSettings> = store.data.catch { emit(UserSettings()) }

  override suspend fun setTheme(theme: ThemePreference) {
    store.updateData { it.copy(theme = theme) }
  }
}

/**
 * JSON on disk. Unknown keys are ignored and missing ones fall back to the
 * data class's defaults, so a settings file written by an older or newer build
 * still parses instead of throwing the user back to defaults wholesale.
 */
internal object UserSettingsSerializer : Serializer<UserSettings> {

  private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

  override val defaultValue = UserSettings()

  override suspend fun readFrom(input: InputStream): UserSettings =
    try {
      json.decodeFromString(UserSettings.serializer(), input.readBytes().decodeToString())
    } catch (e: SerializationException) {
      throw CorruptionException("Settings file is not readable", e)
    }

  override suspend fun writeTo(t: UserSettings, output: OutputStream) {
    output.write(json.encodeToString(UserSettings.serializer(), t).encodeToByteArray())
  }
}
