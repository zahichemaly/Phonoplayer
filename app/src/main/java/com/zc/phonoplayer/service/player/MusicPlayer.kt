package com.zc.phonoplayer.service.player

import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.util.StorageUtil
import com.zc.phonoplayer.util.logD

class MusicPlayer(service: MusicService, var callback: Player.EventListener) {
    private var mExoPlayer: SimpleExoPlayer =
        ExoPlayerFactory.newSimpleInstance(service, DefaultRenderersFactory(service.baseContext), DefaultTrackSelector(), DefaultLoadControl())
    private var mAttrs: AudioAttributes
    private var storageUtil: StorageUtil = StorageUtil(service)
    private var dynamicMediaSource: DynamicMediaSource

    init {
        logD("Initializing music player")
        //mExoPlayer.shuffleModeEnabled = storageUtil.getSavedShuffle()
        mExoPlayer.shuffleModeEnabled = true
        mExoPlayer.repeatMode = storageUtil.getSavedRepeatMode()
        mExoPlayer.addListener(callback)
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MUSIC).build()
        mExoPlayer.setAudioAttributes(mAttrs, true)
        dynamicMediaSource = DynamicMediaSource(service)
    }

    fun prepareUri(uri: Uri, songList: ArrayList<Song>) {
        dynamicMediaSource.addSongsShuffled(uri, songList, storageUtil.getSavedShuffle())
        mExoPlayer.prepare(dynamicMediaSource.getMediaSource())
    }

    fun prepareAll(songList: ArrayList<Song>) {
        val randomStartingIndex = (0..songList.size).random()
        dynamicMediaSource.addSongsShuffled(songList, randomStartingIndex)
        mExoPlayer.prepare(dynamicMediaSource.getMediaSource())
        mExoPlayer.seekTo(randomStartingIndex, 0)
    }

    fun play() {
        mExoPlayer.playWhenReady = true
    }

    fun pause() {
        mExoPlayer.playWhenReady = false
    }

    fun previous() {
        pause()
        mExoPlayer.previous()
        play()
    }

    fun next() {
        pause()
        mExoPlayer.next()
        play()
    }

    fun stop() {
        storageUtil.savePosition(mExoPlayer.currentPosition)
        mExoPlayer.playWhenReady = false
        mExoPlayer.release()
    }

    fun seekTo(position: Long) {
        mExoPlayer.seekTo(position)
    }

    fun isPlaying(): Boolean = mExoPlayer.playbackState == PlaybackStateCompat.STATE_PLAYING
    fun getPosition(): Long = mExoPlayer.currentPosition
    fun getCurrentSong(): Song? = dynamicMediaSource.getSong(mExoPlayer.currentWindowIndex)

    fun setShuffleMode(shuffleMode: Int) {
        logD("On Set Shuffle Mode to $shuffleMode")
        val shuffleModeEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
        storageUtil.saveShuffle(shuffleModeEnabled)
    }

    fun setRepeatMode(repeatMode: Int) {
        logD("On Set Repeat Mode to $repeatMode")
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> mExoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            PlaybackStateCompat.REPEAT_MODE_ONE -> mExoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            PlaybackStateCompat.REPEAT_MODE_ALL -> mExoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            else -> mExoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        }
        storageUtil.saveRepeatMode(repeatMode)
    }
}
