package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zc.phonoplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongFragmentViewModel(application: Application) : FragmentViewModel<Song>(application) {
    private val _permissionToDelete = MutableLiveData<IntentSender>()
    private val _nbOfDeletedSongs = MutableLiveData<Int>()
    var deletedSong: Song? = null
    private var pendingDeleteSong: Song? = null

    fun permissionToDelete(): LiveData<IntentSender> = _permissionToDelete
    fun nbOfDeletedSongs(): LiveData<Int> = _nbOfDeletedSongs

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

    private suspend fun performDeleteSong(song: Song, songIds: Array<Long> = arrayOf(song.id)) {
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
                val selectionArgs: Array<String> = songIds.map { it.toString() }.toTypedArray()
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
                _nbOfDeletedSongs.postValue(getApplication<Application>().contentResolver.delete(uri, selection, selectionArgs))
                deletedSong = song
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException

                    // Signal to the Activity that it needs to request permission and
                    // try the delete again if it succeeds.
                    pendingDeleteSong = song
                    // On Android 10+, if the app doesn't have permission to modify
                    // or delete an item, it returns an `IntentSender` that we can
                    // use here to prompt the user to grant permission to delete (or modify)
                    // the image.
                    _permissionToDelete.postValue(recoverableSecurityException.userAction.actionIntent.intentSender)
                } else {
                    throw securityException
                }
            }
        }
    }
}
