package com.zc.phonoplayer.loader

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.zc.phonoplayer.model.Playlist
import java.util.*

object PlaylistLoader {
    private val URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
    private val PROJECTION = arrayOf(
        MediaStore.Audio.Playlists._ID, //0
        MediaStore.Audio.Playlists.DATA, //1
        MediaStore.Audio.Playlists.NAME //2
    )

    private fun getPlaylistFromCursor(cursor: Cursor): Playlist {
        val id = cursor.getLong(0)
        val data = cursor.getString(1)
        val name = cursor.getString(2)
        return Playlist(id, data, name)
    }

    fun getPlaylists(contentResolver: ContentResolver): ArrayList<Playlist> {
        val playlists = arrayListOf<Playlist>()
        val selection: String? = null
        val selectionArgs: Array<String>? = null
        val sortOrder = MediaStore.Audio.Playlists.NAME + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, selection, selectionArgs, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                playlists.add(getPlaylistFromCursor(cursor))
            }
            cursor.close()
        }
        playlists.forEach { playlist ->
            playlist.songs = SongLoader.getSongsFromPlaylist(contentResolver, playlist.id)
        }
        return playlists
    }
}
