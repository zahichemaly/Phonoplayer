package com.zc.phonoplayer.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

const val ALBUM_PATH: String = "content://media/external/audio/albumart"

class Song(
    var id: Long,
    var data: String?,
    var songTitle: String?,
    var songAlbum: String?,
    var songArtist: String?,
    var songAlbumID: Long,
    var duration: Int,
    var track: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(data)
        parcel.writeString(songTitle)
        parcel.writeString(songAlbum)
        parcel.writeString(songArtist)
        parcel.writeLong(songAlbumID)
        parcel.writeInt(duration)
        parcel.writeInt(track)
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

    fun getUri(): Uri {
        return Uri.parse(this.data)
    }

    fun getAlbumArtUri(): Uri {
        return ContentUris.withAppendedId(Uri.parse(ALBUM_PATH), songAlbumID)
    }
}
