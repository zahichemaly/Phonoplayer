package com.zc.phonoplayer.service.player

import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.room.repository.SongRepository
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.util.PreferenceUtil
import com.zc.phonoplayer.util.SharedPreferencesUtil
import com.zc.phonoplayer.util.logD


class MusicPlayer(val service: MusicService, var callback: Player.EventListener) {
    private var mExoPlayer: SimpleExoPlayer =
        ExoPlayerFactory.newSimpleInstance(service, DefaultRenderersFactory(service.baseContext), DefaultTrackSelector(), DefaultLoadControl())
    private var mAttrs: AudioAttributes
    private var preferenceUtil = PreferenceUtil(service)
    private var sharedPreferencesUtil = SharedPreferencesUtil(service, PreferenceManager.getDefaultSharedPreferences(service.applicationContext))
    private var dynamicMediaSource: DynamicMediaSource
    private var songRepository: SongRepository

    init {
        logD("Initializing music player")
        mExoPlayer.shuffleModeEnabled = true
        mExoPlayer.repeatMode = preferenceUtil.getSavedRepeatMode()
        setPlaybackSpeed(sharedPreferencesUtil.getPlaybackSpeed())
        mExoPlayer.addListener(callback)
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MUSIC).build()
        mExoPlayer.setAudioAttributes(mAttrs, true)
        dynamicMediaSource = DynamicMediaSource(service)
        songRepository = SongRepository(service.applicationContext)
    }

    fun restoreState() {
        val song = preferenceUtil.getSavedSong()
        val songList = songRepository.loadSongs()
        val position = preferenceUtil.getSavedPosition()
        val shuffleMode = preferenceUtil.getSavedShuffle()
        var currentIndex = 0
        if (song != null && !songList.isNullOrEmpty()) {
            dynamicMediaSource.addSongsShuffled(song.getUri(), songList, shuffleMode)
            mExoPlayer.prepare(dynamicMediaSource.getMediaSource(), false, true)
            currentIndex = dynamicMediaSource.getSongIndex(song.getUri())
        }
        if (position != C.TIME_UNSET) {
            mExoPlayer.seekTo(currentIndex, position)
            service.updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, position)
        }
    }

    fun prepareUri(uri: Uri, songList: ArrayList<Song>) {
        dynamicMediaSource.addSongsShuffled(uri, songList, preferenceUtil.getSavedShuffle())
        mExoPlayer.prepare(dynamicMediaSource.getMediaSource(), false, true)
        val startingIndex = dynamicMediaSource.getSongIndex(uri)
        mExoPlayer.seekTo(startingIndex, 0)
    }

    fun prepareAll(songList: ArrayList<Song>) {
        val randomStartingIndex = (0 until songList.size).random()
        dynamicMediaSource.addSongsShuffled(songList, randomStartingIndex)
        mExoPlayer.prepare(dynamicMediaSource.getMediaSource(), false, true)
        mExoPlayer.seekTo(randomStartingIndex, 0)
    }

    fun shuffle(shuffleMode: Int) {
        val uri = getCurrentSong()?.getUri()
        if (uri != null) {
            val shuffleModeEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
            dynamicMediaSource.setShuffleOrder(uri, shuffleModeEnabled)
        }
    }

    fun play() {
        mExoPlayer.playWhenReady = true
    }

    fun pause() {
        mExoPlayer.playWhenReady = false
    }

    fun previous() {
        mExoPlayer.previous()
    }

    fun next() {
        mExoPlayer.next()
    }

    fun stop() {
        val song = getCurrentSong()
        if (song != null) preferenceUtil.saveSong(song)
        preferenceUtil.savePosition(mExoPlayer.currentPosition)
        //preferenceUtil.saveSongList(dynamicMediaSource.getSongList())
        songRepository.insertSongsAsync(dynamicMediaSource.getSongList())
        mExoPlayer.playWhenReady = false
        mExoPlayer.release()
    }

    fun seekTo(position: Long) {
        mExoPlayer.seekTo(position)
    }

    fun isPlaying(): Boolean = mExoPlayer.isPlaying
    fun getPosition(): Long = mExoPlayer.currentPosition
    fun getCurrentSong(): Song? = dynamicMediaSource.getSong(mExoPlayer.currentWindowIndex)

    fun setShuffleMode(shuffleMode: Int) {
        logD("On Set Shuffle Mode to $shuffleMode")
        val shuffleModeEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
        preferenceUtil.saveShuffle(shuffleModeEnabled)
    }

    fun setRepeatMode(repeatMode: Int) {
        logD("On Set Repeat Mode to $repeatMode")
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> mExoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            PlaybackStateCompat.REPEAT_MODE_ONE -> mExoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            PlaybackStateCompat.REPEAT_MODE_ALL -> mExoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            else -> mExoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        }
        preferenceUtil.saveRepeatMode(repeatMode)
    }

    fun getPlaybackSpeed(): Float = mExoPlayer.playbackParameters.speed

    fun setPlaybackSpeed(speed: Float) {
        mExoPlayer.playbackParameters = PlaybackParameters(speed)
    }
}
