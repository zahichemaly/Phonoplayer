package com.zc.phonoplayer.model

class BasicPlaylist(var songList: ArrayList<Song>) {
    private var index = 0;

    init {
        if (songList.isEmpty()) index = -1
    }

    private fun getCurrentSong(index: Int): Song? {
        this.index = index
        return current()
    }

    fun current(): Song? {
        return if (index < 0 || index >= songList.size) {
            null
        } else {
            songList[index]
        }
    }

    fun next(): Song? {
        index++
        if (index >= songList.size) {
            return getCurrentSong(0)
        }
        return getCurrentSong(index)
    }

    fun previous(): Song? {
        index--
        if (index < 0) {
            return getCurrentSong(songList.size - 1)
        }
        return getCurrentSong(index)
    }
}
