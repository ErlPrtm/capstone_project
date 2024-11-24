package com.capstoneproject.aji.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_TOKEN = "token"
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun setToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkModeEnabled(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isEnabled).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
