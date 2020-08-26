package com.zc.phonoplayer.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.zc.phonoplayer.util.ALBUM_PATH
import com.zc.phonoplayer.util.TimeFormatter
import kotlin.math.max

data class Song(
    var id: Long = 0L,
    var data: String? = null,
    var title: String? = null,
    var album: String? = null,
    var albumId: Long = 0L,
    var artist: String? = null,
    var artistId: Long = 0L,
    var duration: Long = 0L,
    var trackNo: Long = 0L,
    var year: Int,
    var albumArtUri: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString()
    )

    init {
        if (this.albumArtUri == null) {
            this.albumArtUri = getAlbumArtUri().toString()
        } else {
            this.albumId = ContentUris.parseId(Uri.parse(albumArtUri))
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(data)
        parcel.writeString(title)
        parcel.writeString(album)
        parcel.writeLong(albumId)
        parcel.writeString(artist)
        parcel.writeLong(artistId)
        parcel.writeLong(duration)
        parcel.writeLong(trackNo)
        parcel.writeInt(year)
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

    fun getUri(): Uri {
        return Uri.parse(this.data)
    }

    fun getAlbumArtUri(): Uri {
        return ContentUris.withAppendedId(Uri.parse(ALBUM_PATH), albumId)
    }

    fun getFormattedDuration(): String? {
        return TimeFormatter.getSongDuration(duration.toInt())
    }

    fun getTrackNo(): String {
        val trackNoWithDiscNo = trackNo.toString()
        val trackNo = trackNoWithDiscNo.substring(max(trackNoWithDiscNo.length - 2, 0))
        return "$trackNo."
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Song) other.data == this.data
        else false
    }
}
