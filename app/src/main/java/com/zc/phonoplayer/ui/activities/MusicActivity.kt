package com.zc.phonoplayer.ui.activities

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.appcompat.app.AppCompatActivity
import com.zc.phonoplayer.util.logD

/**
 * Activity that is connected to a Media  Browser using a session from MusicService::class.java
 * Has a controller callback to update the UI base on the controller state
 * Inherit from this activity to handle media playbacks or take playback actions
 */
abstract class MusicActivity : AppCompatActivity() {
    private lateinit var mMediaBrowser: MediaBrowserCompat
    private var mMediaController: MediaControllerCompat? = null
    protected abstract val mControllerCallback: MediaControllerCompat.Callback
    protected val mConnectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            logD("Controller connected")
            mMediaBrowser.sessionToken.also { token ->
                mMediaController = MediaControllerCompat(this@MusicActivity, token)
                MediaControllerCompat.setMediaController(this@MusicActivity, mMediaController)
            }
            mMediaController?.registerCallback(mControllerCallback)
            setupController()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            logD("Controller connection failed")
        }
    }

    override fun onStart() {
        super.onStart()
        if (!mMediaBrowser.isConnected) mMediaBrowser.connect() //TODO fix connection issues
    }

    override fun onDestroy() {
        mMediaController?.unregisterCallback(mControllerCallback)
        mMediaBrowser.disconnect()
        super.onDestroy()
    }

    abstract fun setupController()
}