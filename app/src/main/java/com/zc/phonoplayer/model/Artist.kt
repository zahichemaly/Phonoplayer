package com.zc.phonoplayer.model

import android.os.Parcel
import android.os.Parcelable

data class Artist(
    var id: Long,
    var title: String? = null,
    var nbOfTracks: Long,
    var nbOfAlbums: Long
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeLong(nbOfTracks)
        parcel.writeLong(nbOfAlbums)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Artist> {
        override fun createFromParcel(parcel: Parcel): Artist {
            return Artist(parcel)
        }

        override fun newArray(size: Int): Array<Artist?> {
            return arrayOfNulls(size)
        }
    }
}
