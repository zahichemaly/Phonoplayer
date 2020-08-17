package com.zc.phonoplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.*

class MusicService : MediaBrowserServiceCompat() {
    private var mExoPlayer: SimpleExoPlayer? = null
    private var mMediaSession: MediaSessionCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var oldUri: Uri? = null
    private var mAttrs: AudioAttributes? = null
    private var currentSong: Song? = null
    private var currentPlaylist: ArrayList<Song>? = null
    private lateinit var dynamicMediaSource: DynamicMediaSource
    private lateinit var storageUtil: StorageUtil

    companion object {
        const val NOTIFICATION_ID = 180018
        const val CHANNEL_ID = "1818181818"
    }

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            logD("On Play From uri $uri")
            currentSong = extras?.getParcelable(SELECTED_SONG) as Song?
            currentPlaylist = extras?.getParcelableArrayList<Song>(SONG_LIST) ?: arrayListOf()
            dynamicMediaSource.addSongs(currentPlaylist!!)
            uri?.let {
                if (uri != oldUri) init()
                else play()
                oldUri = uri
            }
        }

        override fun onPlay() {
            super.onPlay()
            logD("On Play")
            play()
        }

        override fun onPause() {
            super.onPause()
            logD("On Pause")
            pause()
        }

        override fun onStop() {
            super.onStop()
            logD("On Stop")
            stop()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            logD("On Skip To Next")
            next()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            logD("On Skip To Previous")
            previous()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            logD("On Seek To position $pos")
            mExoPlayer?.seekTo(pos)
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            super.onSetShuffleMode(shuffleMode)
            logD("On Set Shuffle Mode to $shuffleMode")
            mExoPlayer?.run {
                shuffleModeEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
                storageUtil.saveShuffle(shuffleModeEnabled)
            }
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
            logD("On Set Repeat Mode to $repeatMode")
            mExoPlayer?.run {
                when (repeatMode) {
                    PlaybackStateCompat.REPEAT_MODE_NONE -> this.repeatMode = Player.REPEAT_MODE_OFF
                    PlaybackStateCompat.REPEAT_MODE_ONE -> this.repeatMode = Player.REPEAT_MODE_ONE
                    PlaybackStateCompat.REPEAT_MODE_ALL -> this.repeatMode = Player.REPEAT_MODE_ALL
                    else -> this.repeatMode = Player.REPEAT_MODE_OFF
                }
            }
            storageUtil.saveRepeatMode(repeatMode)
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            logD("On Command received")
            if (command == "disconnect") {
                stop()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        storageUtil = StorageUtil(this)
        initializePlayer()
        initializeExtractor()
        initializeAttributes()
        mMediaSession = MediaSessionCompat(baseContext, "tag for debugging").apply {
            //setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            mStateBuilder = PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(mStateBuilder.build())
            setCallback(mMediaSessionCallback)
            setSessionToken(sessionToken)
            isActive = true
        }
        mTransportControls = mMediaSession?.controller?.transportControls
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handlePendingIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initializePlayer() {
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultRenderersFactory(baseContext), DefaultTrackSelector(), DefaultLoadControl())
        mExoPlayer!!.shuffleModeEnabled = storageUtil.getSavedShuffle()
        mExoPlayer!!.repeatMode = storageUtil.getSavedRepeatMode()
        mExoPlayer!!.addListener(object : Player.EventListener {
            override fun onPositionDiscontinuity(reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                    currentSong = dynamicMediaSource.getSong(mExoPlayer!!.currentWindowIndex)
                    updateMetadata()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
                    buildNotification(PlaybackStatus.PLAYING)
                }
            }
        })
    }

    private fun init() {
        if (mExoPlayer == null) initializePlayer()
        mExoPlayer?.apply {
            // AudioAttributes here from exoplayer package !!!
            mAttrs?.let { initializeAttributes() }
            // In 2.9.X you don't need to manually handle audio focus :D
            setAudioAttributes(mAttrs, true)
            prepare(dynamicMediaSource.getMediaSource())
            play()
        }
    }

    private fun play() {
        mExoPlayer?.apply {
            playWhenReady = true
            updateMetadata()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, currentPosition)
            buildNotification(PlaybackStatus.PLAYING)
            mMediaSession?.isActive = true
        }
    }

    private fun pause() {
        mExoPlayer?.apply {
            playWhenReady = false
            if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, currentPosition)
                buildNotification(PlaybackStatus.PAUSED)
            }
        }
    }

    private fun stop() {
        if (currentSong != null) {
            storageUtil.saveSong(currentSong!!)
        }
        mExoPlayer?.apply {
            storageUtil.savePosition(currentPosition)
            playWhenReady = false
            release()
        }
        mExoPlayer = null
        updatePlaybackState(PlaybackStateCompat.STATE_NONE, 0L)
        removeNotification()
        mMediaSession?.isActive = false
        mMediaSession?.release()
    }

    private fun previous() {
        mExoPlayer?.apply {
            playWhenReady = false
            previous()
            currentSong = dynamicMediaSource.getSong(currentWindowIndex)
            updateMetadata()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
            buildNotification(PlaybackStatus.PLAYING)
            playWhenReady = true
        }
    }

    private fun next() {
        mExoPlayer?.apply {
            playWhenReady = false
            next()
            currentSong = dynamicMediaSource.getSong(currentWindowIndex)
            updateMetadata()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
            buildNotification(PlaybackStatus.PLAYING)
            playWhenReady = true
        }
    }

    private fun updatePlaybackState(state: Int, position: Long) {
        mMediaSession?.setPlaybackState(PlaybackStateCompat.Builder().setState(state, position, 1.0f).build())
    }

    private fun initializeAttributes() {
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MUSIC).build()
    }

    private fun initializeExtractor() {
        dynamicMediaSource = DynamicMediaSource(this)
    }

    private fun buildNotification(status: PlaybackStatus) {
        var pendingIntent: PendingIntent? = null
        val mainAction: Int
        when (status) {
            PlaybackStatus.PLAYING -> {
                mainAction = R.drawable.exo_notification_pause
                pendingIntent = getPendingIntentFromAction(NotificationAction.PAUSE.value)

            }
            PlaybackStatus.PAUSED -> {
                mainAction = R.drawable.exo_notification_play
                pendingIntent = getPendingIntentFromAction(NotificationAction.PLAY.value)
            }
        }
        val songTitle = currentSong?.title ?: getString(R.string.na)
        val songArtist = currentSong?.artist ?: getString(R.string.na)
        val albumArtBitmap = SongHelper.getBitmapFromUri(currentSong?.getAlbumArtUri(), contentResolver)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setShowWhen(false)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowCancelButton(true)
                    .setMediaSession(mMediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setColor(color(R.color.background_color_dark))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(songArtist)
            .setContentTitle(songTitle)
            .setLargeIcon(albumArtBitmap)
            .addAction(
                R.drawable.exo_notification_previous,
                getString(R.string.action_previous),
                getPendingIntentFromAction(NotificationAction.PREVIOUS.value)
            )
            .addAction(mainAction, getString(R.string.action_play_pause), pendingIntent)
            .addAction(
                R.drawable.exo_notification_next,
                getString(R.string.action_next),
                getPendingIntentFromAction(NotificationAction.NEXT.value)
            )

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
            notificationBuilder.setChannelId(CHANNEL_ID)
        }
        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun removeNotification() {
        logD("Removing notification")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.cancelAll()
        stopForeground(true)
    }

    private fun getPendingIntentFromAction(action: Int): PendingIntent {
        val intent = Intent(this, MusicService::class.java)
        when (action) {
            NotificationAction.PLAY.value -> intent.action = ACTION_PLAY
            NotificationAction.PAUSE.value -> intent.action = ACTION_PAUSE
            NotificationAction.PREVIOUS.value -> intent.action = ACTION_PREVIOUS
            NotificationAction.NEXT.value -> intent.action = ACTION_NEXT
        }
        return PendingIntent.getService(this, action, intent, 0)
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
        mMediaSession?.setMetadata(SongHelper.getMetadataFromSong(currentSong!!))
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
