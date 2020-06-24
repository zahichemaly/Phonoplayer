package com.zc.phonoplayer.loader

import android.content.ContentResolver
import android.provider.MediaStore
import com.zc.phonoplayer.model.Genre
import java.util.*

object GenreLoader {
    private val URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
    private val PROJECTION = arrayOf(
        MediaStore.Audio.Genres._ID,
        MediaStore.Audio.Genres.NAME
    )

    fun getGenreList(contentResolver: ContentResolver): ArrayList<Genre> {
        val genreList = arrayListOf<Genre>()
        val sortOrder = MediaStore.Audio.Genres.NAME + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, null, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)
                genreList.add(Genre(id.toLong(), 0, name))
            }
            cursor.close()
        }
        return genreList
    }
}
