package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.SongHelper

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _song = MutableLiveData<Song>()
    private var _playlist = MutableLiveData<ArrayList<Song>>()
    private var _shuffle = MutableLiveData<Boolean>()
    fun song(): LiveData<Song> = _song
    fun playlist(): LiveData<ArrayList<Song>> = _playlist
    fun shuffle(): LiveData<Boolean> = _shuffle

    fun updateSong(song: Song) {
        _song.value = song
    }

    fun updatePlaylist(selectedSong: Song, songList: ArrayList<Song>) {
        _playlist.value = SongHelper.getDefaultPlaylist(selectedSong, songList)
    }

    fun updateShuffle(isShuffle: Boolean) {
        _shuffle.value = isShuffle
    }
}
