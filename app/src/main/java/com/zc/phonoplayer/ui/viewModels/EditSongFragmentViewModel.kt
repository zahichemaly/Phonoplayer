package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zc.phonoplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditSongFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val _nbOfUpdatedSongs = MutableLiveData<Int>()
    private val _permissionToModify = MutableLiveData<IntentSender>()
    var updatedSong: Song? = null
    private var pendingUpdatedSong: Song? = null

    fun nbOfUpdatedSongs(): LiveData<Int> {
        return _nbOfUpdatedSongs
    }

    fun permissionToModify(): LiveData<IntentSender> {
        return _permissionToModify
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            performUpdateSong(song)
        }
    }

    fun updatePendingSong() {
        pendingUpdatedSong?.let { song ->
            updateSong(song)
        }
    }

    private suspend fun performUpdateSong(song: Song) {
        withContext(Dispatchers.IO) {
            try {
                val selection = "${MediaStore.Audio.Media._ID} = ?"
                val selectionArgs = arrayOf(song.songId.toString())
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.songId)

                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.TITLE, song.title)
                    put(MediaStore.Audio.Media.ALBUM, song.album)
                    put(MediaStore.Audio.Media.ARTIST, song.artist)
                    put(MediaStore.Audio.Media.YEAR, song.year)
                }

                if (Build.VERSION.SDK_INT >= 29) {
                    val tempValues = ContentValues().apply {
                        put(MediaStore.Video.Media.IS_PENDING, 1)
                    }
                    getApplication<Application>().contentResolver.update(uri, tempValues, selection, selectionArgs)
                }

                val updatedValues =
                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues(values).apply {
                            put(MediaStore.Video.Media.IS_PENDING, 0)
                        }
                    } else {
                        values
                    }
                _nbOfUpdatedSongs.postValue(
                    getApplication<Application>().contentResolver.update(
                        uri,
                        updatedValues,
                        selection,
                        selectionArgs
                    )
                )
                updatedSong = song
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException
                    pendingUpdatedSong = song
                    _permissionToModify.postValue(recoverableSecurityException.userAction.actionIntent.intentSender)
                } else {
                    throw securityException
                }
            }

        }
    }
}
