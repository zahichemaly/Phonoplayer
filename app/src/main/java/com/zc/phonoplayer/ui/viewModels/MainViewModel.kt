package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.phonoplayer.model.Song

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _song = MutableLiveData<Song>()

    fun song(): LiveData<Song> {
        return _song
    }

    fun updateSong(song: Song) {
        _song.value = song
    }
}
