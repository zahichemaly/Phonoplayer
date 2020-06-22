package com.zc.phonoplayer.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.zc.phonoplayer.util.ALBUM_PATH

data class Album(
    var id: Long,
    var title: String? = null,
    var artist: String? = null,
    var art: String? = null,
    var nbOfTracks: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(art)
        parcel.writeLong(nbOfTracks)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }

    fun getAlbumArtUri(): Uri {
        return ContentUris.withAppendedId(Uri.parse(ALBUM_PATH), id)
    }

    fun getNbOfTracks(): String {
        return if (nbOfTracks.toInt() != 1) "$nbOfTracks tracks"
        else "$nbOfTracks track"
    }
}
