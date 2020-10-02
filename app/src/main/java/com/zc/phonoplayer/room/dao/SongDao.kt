package com.zc.phonoplayer.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.zc.phonoplayer.model.Song

@Dao
interface SongDao {
    @Query("SELECT * FROM song ORDER BY title ASC")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song WHERE songId IN (:songIds)")
    fun loadAllByIds(songIds: IntArray): List<Song>

    @Query("UPDATE song SET selected = 1 WHERE songId = :songId")
    fun selectSong(songId: Int)

    @Transaction
    fun clearAndInsert(vararg songs: Song) {
        clearAll()
        insertAll(*songs)
    }

    @Insert
    fun insertAll(vararg songs: Song)

    @Query("DELETE FROM song")
    fun clearAll()
}
