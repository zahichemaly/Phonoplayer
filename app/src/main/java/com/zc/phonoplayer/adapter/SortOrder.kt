package com.zc.phonoplayer.adapter

enum class SortOrder(var value: Int) {
    ASCENDING(0),
    DESCENDING(1),
    ARTIST(2),
    ALBUM(3),
    YEAR(4),
    NB_OF_TRACKS(5),
    NB_OF_ALBUMS(6);
}

enum class TrackSortOrder(val value: Int) {
    ASCENDING(0),
    DESCENDING(1),
    ARTIST(2),
    ALBUM(3),
    YEAR(4),
}