package com.zc.phonoplayer.loader

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.zc.phonoplayer.model.Album
import java.util.*

object AlbumLoader {
    private val URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    private val PROJECTION = arrayOf(
        MediaStore.Audio.Albums._ID,  //0
        MediaStore.Audio.Albums.ALBUM,  //1
        MediaStore.Audio.Albums.ARTIST,  //2
        MediaStore.Audio.Albums.ALBUM_ART,  //3
        MediaStore.Audio.Albums.NUMBER_OF_SONGS //4
    )

    private fun getAlbumFromCursor(cursor: Cursor): Album {
        val id = cursor.getLong(0)
        val title = cursor.getString(1)
        val artist = cursor.getString(2)
        val art = cursor.getString(3)
        val tracks = cursor.getLong(4)
        return Album(id, title, artist, art, tracks)
    }

    fun getAlbums(contentResolver: ContentResolver): ArrayList<Album> {
        val albumList = arrayListOf<Album>()
        val sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, null, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                albumList.add(getAlbumFromCursor(cursor))
            }
            cursor.close()
        }
        return albumList
    }

    fun getAlbumsByGenre(contentResolver: ContentResolver, genreId: Int): ArrayList<Album> {
        val albumList = arrayListOf<Album>()
        val sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, null, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                albumList.add(getAlbumFromCursor(cursor))
            }
            cursor.close()
        }
        return albumList
    }
}
