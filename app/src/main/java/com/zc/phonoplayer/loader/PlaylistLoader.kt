package com.zc.phonoplayer.loader

import android.content.ContentResolver
import android.provider.MediaStore
import com.zc.phonoplayer.model.Playlist
import java.util.*

object PlaylistLoader {
    private val URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
    private val PROJECTION = arrayOf(
        MediaStore.Audio.Playlists._ID,
        MediaStore.Audio.Playlists.DATA,
        MediaStore.Audio.Playlists.NAME
    )

    fun getPlaylists(contentResolver: ContentResolver): ArrayList<Playlist> {
        val playlists = arrayListOf<Playlist>()
        val selection: String? = null
        val selectionArgs: Array<String>? = null
        val sortOrder = MediaStore.Audio.Playlists.NAME + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, selection, selectionArgs, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                //val count = cursor.getInt(1)
                val data = cursor.getString(1)
                val name = cursor.getString(2)
                playlists.add(Playlist(id, 1, data, name))
            }
            cursor.close()
        }
        return playlists
    }
}
