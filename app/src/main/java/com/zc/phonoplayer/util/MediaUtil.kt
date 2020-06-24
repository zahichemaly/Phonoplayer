package com.zc.phonoplayer.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.provider.MediaStore
import com.zc.phonoplayer.model.Song

class MediaUtil(private var contentResolver: ContentResolver) {

    fun deleteSong(id: Long) {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        contentResolver.delete(uri, null, null)
    }

    fun updateSong(song: Song) {
        val mediaId = 10
        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(mediaId.toString())
        val updatedSongDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "My Favorite Song.mp3")
            put(MediaStore.Audio.Media.TITLE, "Name")
        }
        val numSongsUpdated = contentResolver.update(
            song.getUri(),
            updatedSongDetails,
            selection,
            selectionArgs)
    }
}
