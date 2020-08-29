package com.zc.phonoplayer.util

import android.content.Context
import android.content.SharedPreferences
import com.google.android.exoplayer2.Player
import com.google.gson.Gson
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song

class PreferenceUtil(val context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("${pkg}_settings", Context.MODE_PRIVATE)

    fun saveSong(song: Song) {
        val json = Gson().toJson(song)
        val editor = prefs.edit()
        editor.putString(context.getString(R.string.pref_key_song), json)
        editor.apply()
    }

    fun getSavedSong(): Song? {
        val json = prefs.getString(context.getString(R.string.pref_key_song), null)
        json?.run {
            return Gson().fromJson<Song>(this)
        } ?: return null
    }

    fun saveSongList(songs: List<Song>) {
        val json = Gson().toJson(songs)
        val editor = prefs.edit()
        editor.putString(context.getString(R.string.pref_key_song_list), json)
        editor.apply()
    }

    fun getSavedSongList(): ArrayList<Song>? {
        val json = prefs.getString(context.getString(R.string.pref_key_song_list), null)
        json?.run {
            return Gson().fromJson<ArrayList<Song>>(this)
        } ?: return null
    }

    fun saveShuffle(isShuffleEnabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(context.getString(R.string.pref_key_shuffle_enabled), isShuffleEnabled)
        editor.apply()
    }

    fun getSavedShuffle(): Boolean {
        return prefs.getBoolean(context.getString(R.string.pref_key_shuffle_enabled), false)
    }

    fun saveRepeatMode(repeatMode: Int) {
        val editor = prefs.edit()
        editor.putInt(context.getString(R.string.pref_key_repeat_mode), repeatMode)
        editor.apply()
    }

    fun getSavedRepeatMode(): Int {
        return prefs.getInt(context.getString(R.string.pref_key_repeat_mode), Player.REPEAT_MODE_OFF)
    }

    fun savePosition(position: Long) {
        val editor = prefs.edit()
        editor.putLong(context.getString(R.string.pref_key_position), position)
        editor.apply()
    }

    fun getSavedPosition(): Long {
        return prefs.getLong(context.getString(R.string.pref_key_position), 0L)
    }

    fun clear() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
