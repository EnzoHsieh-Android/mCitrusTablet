package com.citrus.mCitrusTablet.util

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class TriggerMode {
    START, END
}

class ErrorManager(context: Context) {

    private val dataStore = context.createDataStore(name = "error_pref")

    val uiModeFlow: Flow<TriggerMode> = dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            when (preference[IS_TRIGGER] ?: false) {
                true -> TriggerMode.START
                false -> TriggerMode.END
            }
        }

    suspend fun setTriggerMode(triggerMode: TriggerMode) {
        dataStore.edit { preferences ->
            preferences[IS_TRIGGER] = when (triggerMode) {
                TriggerMode.START -> true
                TriggerMode.END -> false
            }
        }
    }

    companion object {
        val IS_TRIGGER = preferencesKey<Boolean>("trigger_mode")
    }
}