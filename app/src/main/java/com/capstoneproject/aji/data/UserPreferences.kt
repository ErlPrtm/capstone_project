package com.capstoneproject.aji.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[TOKEN_KEY]
            }
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOGGED_IN_KEY] = isLoggedIn
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[LOGGED_IN_KEY] ?: false
            }
    }

    suspend fun setDarkModeEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isEnabled
        }
    }

    fun isDarkModeEnabled(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DARK_MODE_KEY] ?: false
            }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
