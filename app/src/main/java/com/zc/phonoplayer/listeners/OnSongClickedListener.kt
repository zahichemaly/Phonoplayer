package com.zc.phonoplayer.listeners

import com.zc.phonoplayer.model.Song

/**
 * Created by Zahi on 6/11/2020.
 */
interface OnSongClickedListener {
    fun onSongClicked(song: Song)
    fun onSongListReady(songList: ArrayList<Song>)
}
