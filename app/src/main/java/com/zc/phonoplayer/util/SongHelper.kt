package com.zc.phonoplayer.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.palette.graphics.Palette
import com.zc.phonoplayer.model.Song


object SongHelper {
    fun getBitmapFromUri(uri: Uri?, contentResolver: ContentResolver): Bitmap {
        uri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                return ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                return MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } ?: return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    }

    fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

    fun getSongFromMetadata(metadata: MediaMetadataCompat): Song {
        return Song(
            songId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toLong(),
            data = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI),
            title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
            album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM),
            artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
            duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION),
            year = metadata.getLong(MediaMetadataCompat.METADATA_KEY_YEAR).toInt(),
            albumArtUri = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        )
    }

    fun getMetadataFromSong(song: Song): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.songId.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.data)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.getAlbumArtUri().toString())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, song.trackno)
            .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year.toLong())
            .build()
    }
}
