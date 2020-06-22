package com.zc.phonoplayer.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.zc.phonoplayer.model.Song

class StorageUtil(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(STORAGE_LOCATION, Context.MODE_PRIVATE)
    private var editor = prefs.edit()

    companion object {
        const val STORAGE_LOCATION = "cached_songs"
        const val SAVED_SONG = "saved_song"
        const val SAVED_PLAYLIST = "saved_playlist"
        const val SAVED_POSITION = "saved_position"
    }

    fun storeSong(song: Song) {
        val json = Gson().toJson(song)
        editor.putString(SAVED_SONG, json)
        editor.apply()
    }

    fun storePosition(position: Long) {
        editor.putLong(SAVED_POSITION, position)
        editor.apply()
    }

    fun getStoredPosition(): Long {
        return prefs.getLong(SAVED_POSITION, 0L)
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
