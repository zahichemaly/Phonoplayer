package com.zc.phonoplayer.loader

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.zc.phonoplayer.model.Song

object SongLoader {
    private val URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private const val SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"

    @SuppressLint("InlinedApi")
    private val PROJECTION = arrayOf(
        MediaStore.Audio.AudioColumns._ID,  //0
        MediaStore.Audio.AudioColumns.DATA,  //1
        MediaStore.Audio.AudioColumns.TITLE,  //2
        MediaStore.Audio.AudioColumns.ALBUM, //3
        MediaStore.Audio.AudioColumns.ALBUM_ID, //4
        MediaStore.Audio.AudioColumns.ARTIST,  //5
        MediaStore.Audio.AudioColumns.ARTIST_ID, //6
        MediaStore.Audio.AudioColumns.DURATION,  //7
        MediaStore.Audio.AudioColumns.TRACK, //8
        MediaStore.Audio.AudioColumns.YEAR //19
    )

    private fun getSongFromCursor(cursor: Cursor): Song {
        val id = cursor.getLong(0)
        val data = cursor.getString(1)
        val title = cursor.getString(2)
        val album = cursor.getString(3)
        val albumId = cursor.getLong(4)
        val artist = cursor.getString(5)
        val artistId = cursor.getLong(6)
        val duration = cursor.getLong(7)
        val track = cursor.getLong(8)
        val year = cursor.getInt(9)
        return Song(id, data, title, album, albumId, artist, artistId, duration, track, year)
    }

    fun getSongs(contentResolver: ContentResolver): ArrayList<Song> {
        val songList = arrayListOf<Song>()
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, SELECTION, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                songList.add(getSongFromCursor(cursor))
            }
            cursor.close()
        }
        return songList
    }

    fun getSongsFromAlbum(contentResolver: ContentResolver, albumId: Long): ArrayList<Song> {
        val songList = arrayListOf<Song>()
        val selection = SELECTION + " AND " + MediaStore.Audio.AudioColumns.ALBUM_ID + "=" + albumId
        val sortOrder = MediaStore.Audio.Media.TRACK + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, selection, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                songList.add(getSongFromCursor(cursor))
            }
            cursor.close()
        }
        return songList
    }

    fun getArtistSongs(songs: ArrayList<Song>?, artistId: Long): ArrayList<Song> {
        return songs?.filter {
            it.artistId == artistId
        } as ArrayList<Song>
    }

    fun getSongsFromGenre(contentResolver: ContentResolver, genreId: Long): ArrayList<Song> {
        val songList = arrayListOf<Song>()
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(uri, PROJECTION, SELECTION, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                songList.add(getSongFromCursor(cursor))
            }
            cursor.close()
        }
        return songList
    }

    fun getSongsFromPlaylist(contentResolver: ContentResolver, playlistId: Long): ArrayList<Song> {
        val songList = arrayListOf<Song>()
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        val sortOrder = MediaStore.Audio.Media.DATE_ADDED + " ASC"
        val cursor = contentResolver.query(uri, PROJECTION, SELECTION, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                songList.add(getSongFromCursor(cursor))
            }
            cursor.close()
        }
        return songList
    }
}
