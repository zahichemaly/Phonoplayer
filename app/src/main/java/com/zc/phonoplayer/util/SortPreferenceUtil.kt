package com.zc.phonoplayer.util

import android.content.Context
import android.content.SharedPreferences
import com.zc.phonoplayer.R

class SortPreferenceUtil(val context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("${pkg}_sort_settings", Context.MODE_PRIVATE)

    fun getTracksSortOrder(): Int = prefs.getInt(context.getString(R.string.pref_key_tracks_sort), 0)
    fun saveTracksSortOrder(value: Int) {
        val editor = prefs.edit()
        editor.putInt(context.getString(R.string.pref_key_tracks_sort), value)
        editor.apply()
    }
}
