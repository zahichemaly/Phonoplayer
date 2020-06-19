package com.zc.phonoplayer.model

import android.net.Uri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource

class MediaSourcePlaylist(var songList: List<Song>, var mExtractorFactory: ProgressiveMediaSource.Factory) {
    private var mMediaSource: MediaSource? = null
    private var mMediaSourceList: ConcatenatingMediaSource? = null

    init {
        if (songList.isNotEmpty()){
            mMediaSource = extractMediaSourceFromUri(songList.first().getUri())
            mMediaSourceList = ConcatenatingMediaSource(mMediaSource)
            songList.forEach { song ->
                val mediaSource = extractMediaSourceFromUri(song.getUri())
                mMediaSourceList?.addMediaSource(mediaSource)
            }
        }
    }

     private fun extractMediaSourceFromUri(uri: Uri): MediaSource {
        return mExtractorFactory.createMediaSource(uri)
    }
}