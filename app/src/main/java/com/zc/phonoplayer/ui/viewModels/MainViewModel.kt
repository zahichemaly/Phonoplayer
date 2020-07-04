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

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _permissionNeededForDelete = MutableLiveData<IntentSender>()
    private val _nbOfDeletedSongs = MutableLiveData<Int>()
    private val _nbOfUpdatedSongs = MutableLiveData<Int>()
    var pendingDeleteSong: Song? = null

    fun permissionNeededForDelete(): LiveData<IntentSender> {
        return _permissionNeededForDelete
    }

    fun nbOfDeletedSongs(): LiveData<Int> {
        return _nbOfDeletedSongs
    }

    fun nbOfUpdatedSongs(): LiveData<Int> {
        return _nbOfUpdatedSongs
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            performDeleteSong(song)
        }
    }

    fun deletePendingSong() {
        pendingDeleteSong?.let { song ->
            deleteSong(song)
        }
    }

    private suspend fun performDeleteSong(song: Song) {
        withContext(Dispatchers.IO) {
            try {
                /**
                 * In [Build.VERSION_CODES.Q] and above, it isn't possible to modify
                 * or delete items in MediaStore directly, and explicit permission
                 * must usually be obtained to do this.
                 *
                 * The way it works is the OS will throw a [RecoverableSecurityException],
                 * which we can catch here. Inside there's an [IntentSender] which the
                 * activity can use to prompt the user to grant permission to the item
                 * so it can be either updated or deleted.
                 */
                val selection = "${MediaStore.Audio.Media._ID} = ?"
                val selectionArgs = arrayOf(song.id.toString())
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
                _nbOfDeletedSongs.postValue(getApplication<Application>().contentResolver.delete(uri, selection, selectionArgs))
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException

                    // Signal to the Activity that it needs to request permission and
                    // try the delete again if it succeeds.
                    pendingDeleteSong = song
                    _permissionNeededForDelete.postValue(recoverableSecurityException.userAction.actionIntent.intentSender)
                } else {
                    throw securityException
                }
            }
        }
    }

    fun update(newSong: Song) {
        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(newSong.id.toString())
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, newSong.id)

        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.TITLE, newSong.title)
            put(MediaStore.Audio.Media.ALBUM, newSong.album)
            put(MediaStore.Audio.Media.ARTIST, newSong.artist)
            put(MediaStore.Audio.Media.YEAR, newSong.year)
        }

        try {
            getApplication<Application>().contentResolver.update(uri, contentValues, selection, selectionArgs)
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val recoverableSecurityException =
                    securityException as? RecoverableSecurityException
                        ?: throw securityException

                // Signal to the Activity that it needs to request permission and
                // try to update again if it succeeds.
                //_permissionNeededForDelete.postValue(recoverableSecurityException.userAction.actionIntent.intentSender)
            } else {
                throw securityException
            }
        }
    }
}
