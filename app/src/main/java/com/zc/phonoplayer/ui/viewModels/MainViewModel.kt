package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.playlist.PlaylistQueue

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _playlist = MutableLiveData<PlaylistQueue>()
    fun playlist(): LiveData<PlaylistQueue> = _playlist

    fun updatePlaylist(song: Song, songList: ArrayList<Song>) {
        _playlist.value = PlaylistQueue().set(songList).select(song).shuffle(false)
    }

    fun updatePlaylist(songList: ArrayList<Song>) {
        _playlist.value = PlaylistQueue().set(songList).shuffle(true)
    }
}
