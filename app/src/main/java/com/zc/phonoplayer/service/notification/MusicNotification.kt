package com.zc.phonoplayer.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.zc.phonoplayer.R
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.service.PlaybackStatus
import com.zc.phonoplayer.util.*

class MusicNotification {
    private lateinit var service: MusicService
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationManager: NotificationManager
    private lateinit var notification: Notification

    companion object {
        const val NOTIFICATION_ID = 180018
        const val NOTIFICATION_CHANNEL_ID = "1818181818"
    }

    @Synchronized
    fun init(service: MusicService) {
        this.service = service
        notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    fun build(status: PlaybackStatus) {
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
        val song = service.getSong()
        val songTitle = song?.title ?: service.getString(R.string.na)
        val songArtist = song?.artist ?: service.getString(R.string.na)
        val albumArtBitmap = SongHelper.getBitmapFromUri(song?.getAlbumArtUri(), service.contentResolver)

        val notificationBuilder = NotificationCompat.Builder(service,
            NOTIFICATION_CHANNEL_ID
        )
            .setShowWhen(false)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowCancelButton(true)
                    .setMediaSession(service.getMediaSession()?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setColor(service.color(R.color.black))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(songArtist)
            .setContentTitle(songTitle)
            .setLargeIcon(albumArtBitmap)
            .addAction(
                R.drawable.exo_notification_previous,
                service.getString(R.string.action_previous),
                getPendingIntentFromAction(NotificationAction.PREVIOUS.value)
            )
            .addAction(mainAction, service.getString(R.string.action_play_pause), pendingIntent)
            .addAction(
                R.drawable.exo_notification_next,
                service.getString(R.string.action_next),
                getPendingIntentFromAction(NotificationAction.NEXT.value)
            )
        notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
        logD("Starting foreground service")
        service.startForeground(NOTIFICATION_ID, notification)
    }

    private fun getPendingIntentFromAction(action: Int): PendingIntent {
        val intent = Intent(service, MusicService::class.java)
        when (action) {
            NotificationAction.PLAY.value -> intent.action = ACTION_PLAY
            NotificationAction.PAUSE.value -> intent.action = ACTION_PAUSE
            NotificationAction.PREVIOUS.value -> intent.action = ACTION_PREVIOUS
            NotificationAction.NEXT.value -> intent.action = ACTION_NEXT
        }
        return PendingIntent.getService(service, action, intent, 0)
    }

    @Synchronized
    fun remove() {
        logD("Stopping foreground service")
        service.stopForeground(true)
        service.stopSelf()
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        logD("Creating notification channel")
        var notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
