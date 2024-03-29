package com.zc.phonoplayer.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.zc.phonoplayer.R
import com.zc.phonoplayer.ui.components.ManageListPreference

class SharedPreferencesUtil(val context: Context, private val sharedPreferences: SharedPreferences) {
    fun getTheme(): String = sharedPreferences.getString(context.getString(R.string.pref_key_settings_theme), "light") ?: "light"
    fun getAppName(): String {
        return sharedPreferences.getString(context.getString(R.string.pref_key_settings_app_name), context.getString(R.string.app_name))
            ?: context.getString(R.string.app_name)
    }

    fun getTabItems(): List<ManageListPreference.TabItem> {
        val json = sharedPreferences.getString(context.getString(R.string.pref_key_tab_settings_tab), null)
        return if (json != null) {
            Gson().fromJson(json)
        } else {
            val values = context.resources.getStringArray(R.array.tab_entries).toSet()
            return values.map { s -> ManageListPreference.TabItem(s, true) }
        }
    }

    fun setTabItems(values: List<ManageListPreference.TabItem>) {
        val json = Gson().toJson(values)
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.pref_key_tab_settings_tab), json)
        editor.apply()
    }

    fun getPlaybackSpeed(): Float = sharedPreferences.getString(context.getString(R.string.pref_key_settings_play_speed), "1.0")?.toFloat() ?: 1.0f
}
