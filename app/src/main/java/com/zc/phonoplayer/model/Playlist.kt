package com.zc.phonoplayer.model

import android.os.Parcel
import android.os.Parcelable

data class Playlist(
    var id: Long,
    var data: String? = null,
    var name: String? = null,
    var songs: ArrayList<Song>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(Song.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(data)
        parcel.writeString(name)
        parcel.writeTypedList(songs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }

    fun getNbOfTracks(): Int {
        return songs?.size ?: 0
    }

    fun getDisplayedNbOfTracks(): String {
        val nbOfTracks = getNbOfTracks()
        return if (nbOfTracks != 1) "$nbOfTracks tracks"
        else "$nbOfTracks track"
    }
}
