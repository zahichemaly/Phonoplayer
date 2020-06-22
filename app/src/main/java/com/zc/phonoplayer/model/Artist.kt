package com.zc.phonoplayer.model

data class Artist(
    var id: Long,
    var title: String? = null,
    var nbOfTracks: Long,
    var nbOfAlbums: Long
) {
}