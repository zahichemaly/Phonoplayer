package com.zc.phonoplayer.service.playlist

import com.zc.phonoplayer.model.Song

class DefaultPlaylist {
    private var songList: ArrayList<Song> = ArrayList()
    private var selectedIndex: Int = 0
        set(value) {
            field = if (value in songList.indices) value
            else -1
        }
    private var isShuffled: Boolean = false

    fun set(songList: ArrayList<Song>): DefaultPlaylist {
        this.songList = songList
        this.selectedIndex = 0
        return this
    }

    fun select(song: Song): DefaultPlaylist {
        this.selectedIndex = songList.indexOf(song)
        return this
    }

    fun select(index: Int): DefaultPlaylist {
        this.selectedIndex = index
        return this
    }

    fun shuffle(shuffle: Boolean): DefaultPlaylist {
        this.isShuffled = shuffle
        if (shuffle) {
            selectedIndex = (0 until songList.size).random()
        }
        return this
    }

    fun getSelectedSong(): Song? = songList.getOrNull(selectedIndex)
    fun getSongList(): ArrayList<Song> = songList
    fun isShuffled(): Boolean = isShuffled
}
