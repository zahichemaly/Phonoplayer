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
    private var mConcatenatingMediaSource: ConcatenatingMediaSource? = null
    private val userAgent = Util.getUserAgent(context, BuildConfig.APPLICATION_ID)

    fun getMediaSource(): ConcatenatingMediaSource? = mConcatenatingMediaSource
    fun getSongList(): List<Song> = mSongList.toList()

    fun addSongsShuffled(uri: Uri, songs: List<Song>, isShuffled: Boolean = false) {
        mSongList.clear()
        val mediaSources = songs.map {
            mSongList.add(it)
            extractMediaSource(it.getUri())
        }
        if (mConcatenatingMediaSource == null) {
            mConcatenatingMediaSource = ConcatenatingMediaSource(*mediaSources.toTypedArray())
        } else {
            mConcatenatingMediaSource!!.clear()
            mConcatenatingMediaSource!!.addMediaSources(mediaSources)
        }
        setShuffleOrder(uri, isShuffled)
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

    fun setShuffleOrder(uri: Uri, isShuffled: Boolean) {
        val index = getSongIndex(uri)
        if (isShuffled) {
            mConcatenatingMediaSource?.setShuffleOrder(ShuffledOrderFromIndex(mSongList.size, index))
        } else {
            mConcatenatingMediaSource?.setShuffleOrder(SequentialOrder(mSongList.size, index))
        }
    }

    fun getSong(index: Int): Song? {
        val mediaSource = getMediaSource(index)
        return mSongList.firstOrNull { it.getUri() == mediaSource?.tag as Uri }
    }

    private fun getMediaSource(index: Int): MediaSource? {
        return mConcatenatingMediaSource?.getMediaSource(index)
    }

    fun getSongIndex(uri: Uri): Int {
        return mSongList.indexOfFirst { it.getUri() == uri }
    }

    private fun extractMediaSource(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setTag(uri)
            .createMediaSource(uri)
    }
}
