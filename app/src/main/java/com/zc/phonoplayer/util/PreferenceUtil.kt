package com.zc.phonoplayer.util

import android.content.Context
import android.content.SharedPreferences
import com.google.android.exoplayer2.Player
import com.google.gson.Gson
import com.zc.phonoplayer.model.Song

class PreferenceUtil(val context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(STORAGE_LOCATION, Context.MODE_PRIVATE)

    companion object {
        const val STORAGE_LOCATION = "cached_songs"
        const val SAVED_SONG = "saved_song"
        const val SAVED_PLAYLIST = "saved_playlist"
        const val IS_SHUFFLE_ENABLED = "is_shuffle_enabled"
        const val REPEAT_MODE = "repeat_mode"
        const val LAST_POSITION = "last_position"
    }

    fun saveSong(song: Song) {
        val json = Gson().toJson(song)
        val editor = prefs.edit()
        editor.putString(SAVED_SONG, json)
        editor.apply()
    }

    fun saveSongList(songs: List<Song>) {
        val json = Gson().toJson(songs)
        val editor = prefs.edit()
        editor.putString(SAVED_PLAYLIST, json)
        editor.apply()
    }

    fun getSavedSong(): Song? {
        val json = prefs.getString(SAVED_SONG, null)
        json?.run {
            return Gson().fromJson<Song>(this)
        } ?: return null
    }

    fun getSavedSongList(): ArrayList<Song>? {
        val json = prefs.getString(SAVED_PLAYLIST, null)
        json?.run {
            return Gson().fromJson<ArrayList<Song>>(this)
        } ?: return null
    }

    fun saveShuffle(isShuffleEnabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(IS_SHUFFLE_ENABLED, isShuffleEnabled)
        editor.apply()
    }

    fun getSavedShuffle(): Boolean {
        return prefs.getBoolean(IS_SHUFFLE_ENABLED, false)
    }

    fun saveRepeatMode(repeatMode: Int) {
        val editor = prefs.edit()
        editor.putInt(REPEAT_MODE, repeatMode)
        editor.apply()
    }

    fun getSavedRepeatMode(): Int {
        return prefs.getInt(REPEAT_MODE, Player.REPEAT_MODE_OFF)
    }

    fun savePosition(position: Long) {
        val editor = prefs.edit()
        editor.putLong(LAST_POSITION, position)
        editor.apply()
    }

    fun getSavedPosition(): Long {
        return prefs.getLong(LAST_POSITION, 0L)
    }

    fun clear() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
