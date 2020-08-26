package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.playlist.DefaultPlaylist

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _playlist = MutableLiveData<DefaultPlaylist>()
    fun playlist(): LiveData<DefaultPlaylist> = _playlist

    fun updatePlaylist(song: Song, songList: ArrayList<Song>) {
        _playlist.value = DefaultPlaylist().set(songList).select(song).shuffle(false)
    }

    fun updatePlaylist(songList: ArrayList<Song>) {
        _playlist.value = DefaultPlaylist().set(songList).shuffle(true)
    }
}
