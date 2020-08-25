package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.phonoplayer.model.Song

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _song: Song? = null
    private var _playlist = MutableLiveData<ArrayList<Song>>()
    private var _shuffle = MutableLiveData<Boolean>()
    fun playlist(): LiveData<ArrayList<Song>> = _playlist
    fun shuffle(): LiveData<Boolean> = _shuffle

    fun getSong() = _song

    fun updatePlaylist(song: Song, songList: ArrayList<Song>) {
        _song = song
        _playlist.value = songList
    }

    fun updateShuffle(isShuffle: Boolean) {
        _shuffle.value = isShuffle
    }
}
