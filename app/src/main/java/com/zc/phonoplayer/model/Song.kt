package com.zc.phonoplayer.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

const val ALBUM_PATH: String = "content://media/external/audio/albumart"

class Song(
    var id: Long = 0L,
    var data: String? = null,
    var title: String? = null,
    var album: String? = null,
    var artist: String? = null,
    var albumId: Long = 0L,
    var duration: Long = 0L,
    var track: Long = 0L,
    var albumArtUri: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()
    )

    init {
        if (this.albumArtUri == null) {
            this.albumArtUri = getAlbumArtUri().toString()
        } else {
            this.albumId = ContentUris.parseId(Uri.parse(albumArtUri))
        }
    }

    fun getUri(): Uri {
        return Uri.parse(this.data)
    }

    fun getAlbumArtUri(): Uri {
        return ContentUris.withAppendedId(Uri.parse(ALBUM_PATH), albumId)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(data)
        parcel.writeString(title)
        parcel.writeString(album)
        parcel.writeString(artist)
        parcel.writeLong(albumId)
        parcel.writeLong(duration)
        parcel.writeLong(track)
        parcel.writeString(albumArtUri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}
