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

class MediaSourceUtil(context: Context) {
    private var extractorFactory: ProgressiveMediaSource.Factory
    private var concatenatingMediaSource: ConcatenatingMediaSource? = null
    private val size: Int
        get() {
            return concatenatingMediaSource?.size ?: 0
        }
    private var index: Int = 0
        set(value) {
            if (value < 0) field = (concatenatingMediaSource?.size ?: 1) - 1
            else {
                if (concatenatingMediaSource != null) {
                    if (value > concatenatingMediaSource!!.size) field = 0
                } else {
                    field = value
                }
            }
        }

    init {
        val userAgent = Util.getUserAgent(context, BuildConfig.APPLICATION_ID)
        extractorFactory = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
    }

    fun buildMediaSourceList(song: Song?, songList: List<Song>) {
        index = songList.indexOf(song)
        song?.let {
            concatenatingMediaSource = ConcatenatingMediaSource(extractMediaSource(it.getUri()))
        }
        songList.forEach { s ->
            concatenatingMediaSource?.addMediaSource(extractMediaSource(s.getUri()))
        }
    }

    fun current(): MediaSource? {
        return concatenatingMediaSource?.getMediaSource(index)
    }

    fun previous(): MediaSource? {
        index--
        return current()
    }

    fun next(): MediaSource? {
        index++
        return current()
    }

    fun shuffle() {
        concatenatingMediaSource?.setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(size))
    }

    fun unShuffle() {
        concatenatingMediaSource?.setShuffleOrder(ShuffleOrder.UnshuffledShuffleOrder(size))
    }

    fun extractMediaSource(uri: Uri): MediaSource {
        return extractorFactory.createMediaSource(uri)
    }
}
