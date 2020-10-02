package com.zc.phonoplayer.room.repository

import android.content.Context
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.room.AppDatabase
import com.zc.phonoplayer.util.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongRepository(context: Context) {
    private var appDatabase: AppDatabase = AppDatabase.getInstance(context)

    fun insertSongsAsync(songs: List<Song>) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                logD("Inserting queued songs")
                appDatabase.songDao().clearAndInsert(*songs.toTypedArray())
            }
        }
    }

    fun loadSongs(): List<Song> {
        logD("Loading queued songs from DB")
        return appDatabase.songDao().getAll()
    }
}
