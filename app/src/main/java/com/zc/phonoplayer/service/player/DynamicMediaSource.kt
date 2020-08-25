package com.zc.phonoplayer.service.player

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.zc.phonoplayer.BuildConfig
import com.zc.phonoplayer.model.Song

class DynamicMediaSource(var context: Context) {
    private var mSongList: MutableList<Song> = mutableListOf()
    private lateinit var mConcatenatingMediaSource: ConcatenatingMediaSource
    private val userAgent = Util.getUserAgent(context, BuildConfig.APPLICATION_ID)

    fun getMediaSource(): ConcatenatingMediaSource? = mConcatenatingMediaSource

    fun addSongsShuffled(uri: Uri, songs: List<Song>, isShuffled: Boolean = false) {
        mSongList.clear()
        val mediaSources = songs.map {
            mSongList.add(it)
            extractMediaSource(it.getUri())
        }
        mConcatenatingMediaSource = ConcatenatingMediaSource(*mediaSources.toTypedArray())
        val startIndex = getSongIndex(uri)
        if (isShuffled) {
            val shuffleOrder = ShuffledOrderFromIndex(mSongList.size, startIndex)
            mConcatenatingMediaSource.setShuffleOrder(shuffleOrder)
        } else {
            mConcatenatingMediaSource.setShuffleOrder(SequentialOrder(mSongList.size, startIndex))
        }
    }

    fun addSongsShuffled(songs: List<Song>, startingIndex: Int) {
        mSongList.clear()
        val mediaSources = songs.map {
            mSongList.add(it)
            extractMediaSource(it.getUri())
        }
        val shuffleOrder = ShuffledOrderFromIndex(mSongList.size, startingIndex)
        mConcatenatingMediaSource = ConcatenatingMediaSource(false, shuffleOrder, *mediaSources.toTypedArray())
    }

    fun getSong(index: Int): Song? {
        val mediaSource = getMediaSource(index)
        return mSongList.firstOrNull { it.getUri() == mediaSource?.tag as Uri }
    }

    private fun getMediaSource(index: Int): MediaSource? {
        return mConcatenatingMediaSource.getMediaSource(index)
    }

    private fun getSongIndex(uri: Uri): Int {
        return mSongList.indexOfFirst { it.getUri() == uri }
    }

    private fun extractMediaSource(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setTag(uri)
            .createMediaSource(uri)
    }
}
