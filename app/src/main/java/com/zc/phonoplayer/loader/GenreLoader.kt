package com.zc.phonoplayer.loader

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.zc.phonoplayer.model.Genre
import java.util.*

object GenreLoader {
    private val URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
    private val PROJECTION = arrayOf(
        MediaStore.Audio.Genres._ID,
        MediaStore.Audio.Genres.NAME
    )

    private fun getGenreFromCursor(cursor: Cursor): Genre {
        val id = cursor.getLong(0)
        val name = cursor.getString(1)
        return Genre(id, 0, name)
    }

    fun getGenreList(contentResolver: ContentResolver): ArrayList<Genre> {
        val genreList = arrayListOf<Genre>()
        val sortOrder = MediaStore.Audio.Genres.NAME + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, null, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                genreList.add(getGenreFromCursor(cursor))
            }
            cursor.close()
        }
        return genreList
    }
}
