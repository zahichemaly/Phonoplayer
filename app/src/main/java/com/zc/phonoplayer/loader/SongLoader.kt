package com.zc.phonoplayer.loader

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.MediaStore
import com.zc.phonoplayer.model.Song
import java.util.*

object SongLoader {
    private val URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private const val SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"

    @SuppressLint("InlinedApi")
    private val PROJECTION = arrayOf(
        MediaStore.Audio.AudioColumns._ID,  //0
        MediaStore.Audio.AudioColumns.DATA,  //1
        MediaStore.Audio.AudioColumns.TITLE,  //2
        MediaStore.Audio.AudioColumns.ALBUM,  //3
        MediaStore.Audio.AudioColumns.ARTIST,  //4
        MediaStore.Audio.AudioColumns.ALBUM_ID,  //5
        MediaStore.Audio.AudioColumns.DURATION,  //6
        MediaStore.Audio.AudioColumns.TRACK //7
    )

    fun getSongs(contentResolver: ContentResolver): ArrayList<Song> {
        val songList = arrayListOf<Song>()
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, SELECTION, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val data = cursor.getString(1)
                val title = cursor.getString(2)
                val album = cursor.getString(3)
                val artist = cursor.getString(4)
                val albumId = cursor.getLong(5)
                val duration = cursor.getLong(6)
                val track = cursor.getLong(7)
                songList.add(Song(id, data, title, album, artist, albumId, duration, track))
            }
            cursor.close()
        }
        return songList
    }

    fun getSongsFromAlbum(contentResolver: ContentResolver, albumID: String): ArrayList<Song> {
        val songList = arrayListOf<Song>()
        val selection = SELECTION + " AND " + MediaStore.Audio.AudioColumns.ALBUM_ID + "=" + albumID
        val sortOrder = MediaStore.Audio.Media.TRACK + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, selection, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val data = cursor.getString(1)
                val title = cursor.getString(2)
                val album = cursor.getString(3)
                val artist = cursor.getString(4)
                val albumId = cursor.getLong(5)
                val duration = cursor.getLong(6)
                val track = cursor.getLong(7)
                songList.add(Song(id, data, title, album, artist, albumId, duration, track))
            }
            cursor.close()
        }
        return songList
    }

    fun getSongsFromGenre(contentResolver: ContentResolver, genreId: Int): ArrayList<Song> {
        val songList = arrayListOf<Song>()
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId.toLong())
        val sortOrder = MediaStore.Audio.Media.TRACK + " ASC"
        val cursor = contentResolver.query(uri, PROJECTION, SELECTION, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val data = cursor.getString(1)
                val title = cursor.getString(2)
                val album = cursor.getString(3)
                val artist = cursor.getString(4)
                val albumId = cursor.getLong(5)
                val duration = cursor.getLong(6)
                val track = cursor.getLong(7)
                songList.add(Song(id, data, title, album, artist, albumId, duration, track))
            }
            cursor.close()
        }
        return songList
    }
}
