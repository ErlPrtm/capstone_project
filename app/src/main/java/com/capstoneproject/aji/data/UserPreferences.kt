package com.capstoneproject.aji.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.capstoneproject.aji.data.model.AttendanceLog
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
        private val ROLE_KEY = stringPreferencesKey("role")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val STATUS_ABSENCE_KEY = stringPreferencesKey("status_absence")
        private val LAST_ABSENCE_DATE_KEY = stringPreferencesKey("last_absence_date")
        private val USER_DETAILS_PREFIX = "user_detail_"
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data
            .catchException()
            .map { preferences -> preferences[TOKEN_KEY] }
    }

    suspend fun saveUserId(userId: Int) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    fun getUserId(): Flow<Int?> {
        return dataStore.data
            .catchException()
            .map { preferences -> preferences[USER_ID_KEY] }
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOGGED_IN_KEY] = isLoggedIn
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data
            .catchException()
            .map { preferences -> preferences[LOGGED_IN_KEY] ?: false }
    }

    fun getStatusAbsence(): Flow<String?> {
        return dataStore.data
            .map { preferences -> preferences[STATUS_ABSENCE_KEY] }
    }

    suspend fun setStatusAbsence(status: String) {
        dataStore.edit { preferences ->
            preferences[STATUS_ABSENCE_KEY] = status
        }
    }

    fun getLastAbsenceData(): Flow<String?> {
        return dataStore.data
            .map { preferences -> preferences[LAST_ABSENCE_DATE_KEY] }
    }

    suspend fun setLastAbsenceData(date: String) {
        dataStore.edit { preferences ->
            preferences[LAST_ABSENCE_DATE_KEY] = date
        }
    }

    suspend fun setDarkModeEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isEnabled
        }
    }

    fun isDarkModeEnabled(): Flow<Boolean> {
        return dataStore.data
            .catchException()
            .map { preferences -> preferences[DARK_MODE_KEY] ?: false }
    }

    suspend fun saveRole(role: String) {
        dataStore.edit { preferences ->
            preferences[ROLE_KEY] = role
        }
    }

    fun getRole(): Flow<String?> {
        return dataStore.data
            .catchException()
            .map { preferences -> preferences[ROLE_KEY] }
    }

    suspend fun saveUserDetailsFromResponse(user: Map<String, String>) {
        dataStore.edit { preferences ->
            user.forEach { (key, value) ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    fun getUserDetail(key: String): Flow<String?> {
        return dataStore.data
            .catchException()
            .map { preferences -> preferences[stringPreferencesKey(key)] }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(LOGGED_IN_KEY)
            preferences.remove(DARK_MODE_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }

    private fun <T> Flow<T>.catchException(): Flow<T> {
        return this.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences() as T)
            } else {
                throw exception
            }
        }
    }

}
