package com.zc.phonoplayer.util

import android.content.ContentResolver
import com.zc.phonoplayer.loader.*
import com.zc.phonoplayer.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

object GlobalSongLoader {
    var songs = ArrayList<Song>()
    var albums = ArrayList<Album>()
    var artists = ArrayList<Artist>()
    var genres = ArrayList<Genre>()
    var playlists = ArrayList<Playlist>()

    private suspend fun loadSongsAsync(contentResolver: ContentResolver) {
        coroutineScope {
            val data = listOf(
                async { songs = SongLoader.getSongs(contentResolver) },
                async { albums = AlbumLoader.getAlbums(contentResolver) },
                async { artists = ArtistLoader.getArtistList(contentResolver) },
                async { genres = GenreLoader.getGenreList(contentResolver) },
                async { playlists = PlaylistLoader.getPlaylists(contentResolver) }
            )
            data.awaitAll()
        }
    }

    fun loadSongs(contentResolver: ContentResolver) {
        runBlocking {
            loadSongsAsync(contentResolver)
        }
    }
}
