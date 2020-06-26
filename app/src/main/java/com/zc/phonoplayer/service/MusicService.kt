package com.zc.phonoplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.BasicPlaylist
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
    private var defaultBasicPlaylist: BasicPlaylist? = null
    private var currentSong: Song? = null
    private lateinit var mediaSourceUtil: MediaSourceUtil
    private lateinit var mMediaSource: MediaSource

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "1818181818"
    }

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            currentSong = extras?.getParcelable(SELECTED_SONG) as Song?
            val songList = extras?.getParcelableArrayList<Song>(SONG_LIST) ?: arrayListOf<Song>()
            defaultBasicPlaylist = BasicPlaylist(songList)
            uri?.let {
                mMediaSource = mediaSourceUtil.extractMediaSource(uri)
                if (uri != oldUri) play(mMediaSource)
                else play() // this song was paused so we don't need to reload it
                oldUri = uri
            }
        }

        override fun onPlay() {
            super.onPlay()
            play()
        }

        override fun onPause() {
            super.onPause()
            pause()
        }

        override fun onStop() {
            super.onStop()
            stop()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            previous()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            next()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            mExoPlayer?.seekTo(pos)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeExtractor()
        initializeAttributes()
        mMediaSession = MediaSessionCompat(baseContext, "tag for debugging").apply {
            //setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            mStateBuilder =
                PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
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
        mExoPlayer =
            ExoPlayerFactory.newSimpleInstance(this, DefaultRenderersFactory(baseContext), DefaultTrackSelector(), DefaultLoadControl())
    }

    private fun play(mediaSource: MediaSource) {
        if (mExoPlayer == null) initializePlayer()
        mExoPlayer?.apply {
            // AudioAttributes here from exoplayer package !!!
            mAttrs?.let { initializeAttributes() }
            // In 2.9.X you don't need to manually handle audio focus :D
            setAudioAttributes(mAttrs, true)
            updateMetadata()
            prepare(mediaSource)
            play()
        }
    }

    private fun play() {
        mExoPlayer?.apply {
            mExoPlayer?.playWhenReady = true
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
        mExoPlayer?.playWhenReady = false
        mExoPlayer?.release()
        mExoPlayer = null
        updatePlaybackState(PlaybackStateCompat.STATE_NONE, 0L)
        removeNotification()
        mMediaSession?.isActive = false
        mMediaSession?.release()
    }

    private fun previous() {
        mExoPlayer?.apply {
            mExoPlayer?.playWhenReady = false
            currentSong = defaultBasicPlaylist?.previous()
            currentSong?.let {
                val source = mediaSourceUtil.extractMediaSource(it.getUri())
                play(source)
            }
        }
    }

    private fun next() {
        mExoPlayer?.apply {
            mExoPlayer?.playWhenReady = false
            currentSong = defaultBasicPlaylist?.next()
            currentSong?.let {
                val source = mediaSourceUtil.extractMediaSource(it.getUri())
                play(source)
            }
        }
    }

    private fun updatePlaybackState(state: Int, position: Long) {
        mMediaSession?.setPlaybackState(PlaybackStateCompat.Builder().setState(state, position, 1.0f).build())
    }

    private fun initializeAttributes() {
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

    private fun initializeExtractor() {
        mediaSourceUtil = MediaSourceUtil(this)
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
            .setSmallIcon(android.R.drawable.stat_sys_headset)
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
            val channelId = "channel_id_898989"
            val notificationChannel = NotificationChannel(channelId, "channel_name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
            notificationBuilder.setChannelId(channelId)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.cancelAll()
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
