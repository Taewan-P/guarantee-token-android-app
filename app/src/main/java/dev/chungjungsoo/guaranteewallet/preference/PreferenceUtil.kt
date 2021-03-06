package dev.chungjungsoo.guaranteewallet.preference

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("login_token", Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String?): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun resetToken() {
        prefs.edit().remove("jwt").apply()
        prefs.edit().remove("account").apply()
        prefs.edit().remove("type").apply()
        prefs.edit().remove("key").apply()
    }
}
