package com.zc.phonoplayer.loader

import android.content.ContentResolver
import android.provider.MediaStore
import com.zc.phonoplayer.model.Artist
import java.util.*

object ArtistLoader {
    private val URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    private val PROJECTION = arrayOf(
        MediaStore.Audio.Artists._ID,
        MediaStore.Audio.Artists.ARTIST,
        MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
    )

    fun getArtistList(contentResolver: ContentResolver): ArrayList<Artist> {
        val artistList = ArrayList<Artist>()
        val sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC"
        val cursor = contentResolver.query(URI, PROJECTION, null, null, sortOrder)
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val nbOfTracks = cursor.getInt(2)
                val nbOfAlbums = cursor.getInt(3)
                artistList.add(Artist(id, title, nbOfTracks.toLong(), nbOfAlbums.toLong()))
            }
            cursor.close()
        }
        return artistList
    }
}
