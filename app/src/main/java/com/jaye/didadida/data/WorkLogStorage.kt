package com.jaye.didadida.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.jaye.didadida.domain.WorkLog
import com.jaye.didadida.domain.SettingsConfig

private val Context.dataStore by preferencesDataStore(name = "didadida")

class WorkLogStorage(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val workLogsKey = stringPreferencesKey("work_logs")
    private val settingsKey = stringPreferencesKey("settings")

    // === Work Logs ===

    suspend fun saveWorkLog(log: WorkLog) {
        val logs = loadAllWorkLogs().toMutableMap()
        logs[log.date.toString()] = log
        persistWorkLogs(logs)
    }

    suspend fun deleteWorkLog(date: LocalDate) {
        val logs = loadAllWorkLogs().toMutableMap()
        logs.remove(date.toString())
        persistWorkLogs(logs)
    }

    suspend fun loadAllWorkLogs(): Map<String, WorkLog> {
        val raw = context.dataStore.data.first()[workLogsKey] ?: return emptyMap()
        return try {
            json.decodeFromString<Map<String, WorkLog>>(raw)
        } catch (_: Exception) { emptyMap() }
    }

    fun allWorkLogsFlow(): Flow<Map<String, WorkLog>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[workLogsKey] ?: return@map emptyMap()
            try { json.decodeFromString<Map<String, WorkLog>>(raw) }
            catch (_: Exception) { emptyMap() }
        }

    suspend fun getWorkLog(date: LocalDate): WorkLog? =
        loadAllWorkLogs()[date.toString()]

    private suspend fun persistWorkLogs(logs: Map<String, WorkLog>) {
        context.dataStore.edit { prefs ->
            prefs[workLogsKey] = json.encodeToString(logs)
        }
    }

    // === Settings ===

    suspend fun saveSettings(settings: SettingsConfig) {
        context.dataStore.edit { prefs ->
            prefs[settingsKey] = json.encodeToString(settings)
        }
    }

    suspend fun loadSettings(): SettingsConfig {
        val raw = context.dataStore.data.first()[settingsKey] ?: return SettingsConfig()
        return try {
            json.decodeFromString<SettingsConfig>(raw)
        } catch (_: Exception) { SettingsConfig() }
    }

    fun settingsFlow(): Flow<SettingsConfig> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[settingsKey] ?: return@map SettingsConfig()
            try { json.decodeFromString<SettingsConfig>(raw) }
            catch (_: Exception) { SettingsConfig() }
        }
}
