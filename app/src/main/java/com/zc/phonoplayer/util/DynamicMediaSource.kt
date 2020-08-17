package com.zc.phonoplayer.util

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ShuffleOrder
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.zc.phonoplayer.BuildConfig
import com.zc.phonoplayer.model.Song

class DynamicMediaSource(var context: Context) {
    private var mSongList: MutableList<Song> = mutableListOf()
    private var mConcatenatingMediaSource: ConcatenatingMediaSource? = null
    private val userAgent = Util.getUserAgent(context, BuildConfig.APPLICATION_ID)
    private val size = mConcatenatingMediaSource?.size ?: 0

    fun getMediaSource(): ConcatenatingMediaSource? = mConcatenatingMediaSource

    fun setPlaylist(songList: List<Song>) {
        mSongList.addAll(songList)
    }

    fun addSong(song: Song) {
        mSongList.add(song)
        if (mConcatenatingMediaSource == null) mConcatenatingMediaSource = ConcatenatingMediaSource(extractMediaSource(song.getUri()))
        else mConcatenatingMediaSource!!.addMediaSource(extractMediaSource(song.getUri()))
    }

    fun addSongs(songs: List<Song>) {
        val mediaSources = songs.map {
            mSongList.add(it)
            extractMediaSource(it.getUri())
        }
        mConcatenatingMediaSource = ConcatenatingMediaSource(*mediaSources.toTypedArray())
    }

    fun moveToFirst(song: Song) {
        mConcatenatingMediaSource?.moveMediaSource(mSongList.indexOf(song), 0)
    }

    private fun createShuffleOrder() {
    }

    fun next() {
        removeSong(0)
    }

    fun previous() {
        val lastIndex = size - 1
        if (lastIndex > 0) {
            removeSong(lastIndex)
        }
    }

    fun getMediaSource(index: Int): MediaSource? {
        return mConcatenatingMediaSource?.getMediaSource(index)
    }

    fun getSong(index: Int): Song? {
        val mediaSource = getMediaSource(index)
        return mSongList.firstOrNull { it.getUri() == mediaSource?.tag as Uri }
    }

    fun removeSong(index: Int) {
        if (size > 0) {
            mSongList.removeAt(index)
            mConcatenatingMediaSource?.removeMediaSource(index)
        }
    }

    fun clear() {
        mSongList.clear()
        mConcatenatingMediaSource?.clear()
    }

    fun shuffle() = mConcatenatingMediaSource?.setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(mConcatenatingMediaSource?.size ?: 0))

    fun unShuffle() = mConcatenatingMediaSource?.setShuffleOrder(ShuffleOrder.UnshuffledShuffleOrder(mConcatenatingMediaSource?.size ?: 0))

    private fun extractMediaSource(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setTag(uri)
            .createMediaSource(uri)
    }
}
