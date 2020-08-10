package com.zc.phonoplayer.loader

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import com.zc.phonoplayer.model.Album

object AlbumLoader {
    private val URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    private val PROJECTION = arrayOf(
        MediaStore.Audio.Albums._ID,  //0
        MediaStore.Audio.Albums.ALBUM,  //1
        MediaStore.Audio.Albums.ARTIST,  //2
        MediaStore.Audio.Albums.ALBUM_ART,  //3
        MediaStore.Audio.Albums.NUMBER_OF_SONGS, //4
        MediaStore.Audio.Albums.FIRST_YEAR //5
    )

    private fun getAlbumFromCursor(cursor: Cursor): Album {
        val id = cursor.getLong(0)
        val title = cursor.getString(1)
        val artist = cursor.getString(2)
        val art = cursor.getString(3)
        val tracks = cursor.getLong(4)
        val year = cursor.getInt(5)
        return Album(id, title, artist, art, tracks, year)
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

    fun getAlbumById(contentResolver: ContentResolver, albumId: Long): Album? {
        var album: Album? = null
        val sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"
        val uri = ContentUris.withAppendedId(URI, albumId)
        contentResolver.query(uri, PROJECTION, null, null, sortOrder)?.apply {
            while (moveToNext()) {
                album = getAlbumFromCursor(this)
                break
            }
            close()
        }
        return album
    }
}
