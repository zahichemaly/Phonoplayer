package com.zc.phonoplayer.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zc.phonoplayer.util.ALBUM_PATH
import com.zc.phonoplayer.util.TimeFormatter
import kotlin.math.max

@Entity
data class Song(
    @PrimaryKey var songId: Long = 0L,
    @ColumnInfo(name = "data") var data: String? = "",
    @ColumnInfo(name = "title") var title: String? = "N/A",
    @ColumnInfo(name = "album_title") var album: String? = "N/A",
    @SerializedName("albumId")
    @ColumnInfo(name = "album_id") var albumId: Long = 0L,
    @ColumnInfo(name = "artist_title") var artist: String? = "N/A",
    @ColumnInfo(name = "artist_id") var artistId: Long = 0L,
    @ColumnInfo(name = "duration") var duration: Long = 0L,
    @ColumnInfo(name = "track_no") var trackno: Long = 0L,
    @ColumnInfo(name = "year") var year: Int,
    @Ignore var albumArtUri: String? = "",
    @ColumnInfo(name = "selected") var selected: Boolean = false
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
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    constructor() : this(0, "", "", "", 0, "", 0, 0, 0, 0, "", false)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(songId)
        parcel.writeString(data)
        parcel.writeString(title)
        parcel.writeString(album)
        parcel.writeLong(albumId)
        parcel.writeString(artist)
        parcel.writeLong(artistId)
        parcel.writeLong(duration)
        parcel.writeLong(trackno)
        parcel.writeInt(year)
        parcel.writeString(albumArtUri)
        parcel.writeByte(if (selected) 1 else 0)
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
        return if (albumArtUri.isNullOrEmpty()) ContentUris.withAppendedId(Uri.parse(ALBUM_PATH), albumId)
        else Uri.parse(albumArtUri)
    }

    fun getFormattedDuration(): String? {
        return TimeFormatter.getSongDuration(duration.toInt())
    }

    fun getTrackNo(): String {
        val trackNoWithDiscNo = trackno.toString()
        return trackNoWithDiscNo.substring(max(trackNoWithDiscNo.length - 2, 0))
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Song) other.data == this.data
        else false
    }

    override fun hashCode(): Int {
        var result = songId.hashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + albumId.hashCode()
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + artistId.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + trackno.hashCode()
        result = 31 * result + year
        result = 31 * result + (albumArtUri?.hashCode() ?: 0)
        result = 31 * result + selected.hashCode()
        return result
    }
}
