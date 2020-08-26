package com.zc.phonoplayer.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.notification.MusicNotification
import com.zc.phonoplayer.service.player.MusicPlayer
import com.zc.phonoplayer.util.*

class MusicService : MediaBrowserServiceCompat() {
    private var mMediaSession: MediaSessionCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    private var song: Song? = null
    private var songList: ArrayList<Song>? = null
    private lateinit var musicNotification: MusicNotification
    private lateinit var musicPlayer: MusicPlayer
    fun getSong(): Song? = song
    fun getMediaSession(): MediaSessionCompat? = mMediaSession
    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            logD("On Play From uri $uri")
            song = extras?.getParcelable(SELECTED_SONG) as Song?
            songList = extras?.getParcelableArrayList<Song>(SONG_LIST) ?: arrayListOf()
            if (musicPlayer.isRestored) {
                musicPlayer.isRestored = false
            } else {
                musicPlayer.prepareUri(uri!!, songList!!)
            }
            musicPlayer.play()
            updateMetadata()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, musicPlayer.getPosition())
            musicNotification.build(PlaybackStatus.PLAYING)
            mMediaSession?.isActive = true
        }

        override fun onPlay() {
            super.onPlay()
            play()
        }

        override fun onPause() {
            super.onPause()
            musicPlayer.pause()
            if (musicPlayer.isPlaying()) {
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, musicPlayer.getPosition())
                musicNotification.build(PlaybackStatus.PAUSED)
            }
        }

        override fun onStop() {
            super.onStop()
            stop()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            skip(SkipStatus.SKIPPED_NEXT)
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            skip(SkipStatus.SKIPPED_PREVIOUS)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            musicPlayer.seekTo(pos)
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            super.onSetShuffleMode(shuffleMode)
            musicPlayer.setShuffleMode(shuffleMode)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
            musicPlayer.setRepeatMode(repeatMode)
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            logD("On Command received $command")
            if (command == COMMAND_DISCONNECT) {
                stop()
            }
            if (command == COMMAND_SHUFFLE_ALL) {
                songList = extras?.getParcelableArrayList<Song>(SONG_LIST) ?: arrayListOf()
                musicPlayer.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
                musicPlayer.prepareAll(songList!!)
                play()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        musicNotification = MusicNotification()
        musicNotification.init(this)
        initializePlayer()
        mMediaSession = MediaSessionCompat(baseContext, "tag for debugging").apply {
            mStateBuilder = PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(mStateBuilder.build())
            setCallback(mMediaSessionCallback)
            setSessionToken(sessionToken)
            isActive = true
        }
        mTransportControls = mMediaSession?.controller?.transportControls
        musicPlayer.restoreState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handlePendingIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initializePlayer() {
        musicPlayer = MusicPlayer(this, object : Player.EventListener {
            override fun onPositionDiscontinuity(reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                    song = musicPlayer.getCurrentSong()
                    logD("Playback ended - Next song $song")
                    updateMetadata()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
                    musicNotification.build(PlaybackStatus.PLAYING)
                }
            }
        })
    }

    private fun play() {
        musicPlayer.play()
        song = musicPlayer.getCurrentSong()
        updateMetadata()
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, musicPlayer.getPosition())
        musicNotification.build(PlaybackStatus.PLAYING)
        mMediaSession?.isActive = true
    }

    private fun skip(skipStatus: SkipStatus) {
        when (skipStatus) {
            SkipStatus.SKIPPED_PREVIOUS -> musicPlayer.previous()
            SkipStatus.SKIPPED_NEXT -> musicPlayer.next()
        }
        song = musicPlayer.getCurrentSong()
        logD("$skipStatus to $song")
        updateMetadata()
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
        musicNotification.build(PlaybackStatus.PLAYING)
    }

    private fun stop() {
        logD("Stopping music service")
        musicPlayer.stop()
        updatePlaybackState(PlaybackStateCompat.STATE_NONE, 0L)
        musicNotification.remove()
        mMediaSession?.isActive = false
        mMediaSession?.release()
    }

    fun updatePlaybackState(state: Int, position: Long) {
        mMediaSession?.setPlaybackState(PlaybackStateCompat.Builder().setState(state, position, 1.0f).build())
    }

    private fun handlePendingIntent(intent: Intent?) {
        intent?.run {
            when (action) {
                ACTION_PLAY -> mTransportControls?.play()
                ACTION_PAUSE -> mTransportControls?.pause()
                ACTION_PREVIOUS -> mTransportControls?.skipToPrevious()
                ACTION_NEXT -> mTransportControls?.skipToNext()
                else -> return
            }
        }
    }

    private fun updateMetadata() {
        if (song != null) {
            mMediaSession?.setMetadata(SongHelper.getMetadataFromSong(song!!))
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
