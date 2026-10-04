package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("labour_bill_prefs", Context.MODE_PRIVATE)

    var currentUserId: Long
        get() = prefs.getLong(KEY_CURRENT_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_CURRENT_USER_ID, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()

    fun clearSession() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
    }
}
