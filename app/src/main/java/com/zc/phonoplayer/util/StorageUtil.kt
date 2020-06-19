package com.zc.phonoplayer.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.zc.phonoplayer.model.Song

class StorageUtil(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(STORAGE_LOCATION, Context.MODE_PRIVATE)
    private var editor = prefs.edit()

    fun storeSong(song: Song) {
        val json = Gson().toJson(song)
        editor.putString(SAVED_SONG, json)
        editor.apply()
    }

    fun getStoredSong(): Song? {
        val json = prefs.getString(SAVED_SONG, null)
        json?.run {
            return Gson().fromJson<Song>(this)
        } ?: return null
    }

    fun clear() {
        editor.clear()
        editor.apply()
    }
}
