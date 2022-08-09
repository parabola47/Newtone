package com.parabola.player_feature

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.parabola.player_feature.PlayerInteractorImpl.NewtonePlayerListener


class PlayerService : Service() {

    private var notificationId = 0
    private var notification: Notification? = null

    override fun onCreate() {
        playerInteractor!!.setNewtonePlayerListener(object : NewtonePlayerListener {

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean,
            ) {
                this@PlayerService.notificationId = notificationId
                this@PlayerService.notification = notification

                val intent = Intent(this@PlayerService, PlayerService::class.java)
                    .setAction(if (ongoing) ACTION_START_FOREGROUND else ACTION_STOP_FOREGROUND)

                if (ongoing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    try {
                        startService(intent)
                    } catch (e: IllegalStateException) {
                        if (ongoing) startForeground(notificationId, notification)
                        else stopForeground(false)
                    }
                }
            }

            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                val intent = Intent(this@PlayerService, PlayerService::class.java)
                    .setAction(ACTION_STOP_SERVICE)

                try {
                    startService(intent)
                } catch (e: IllegalStateException) {
                    stopForeground(true)
                    stopSelf()
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null)
            handleAction(intent.action!!)

        return START_STICKY
    }

    private fun handleAction(action: String) {
        when (action) {
            ACTION_TOGGLE_PLAYING -> playerInteractor!!.toggle()
            ACTION_NEXT -> playerInteractor!!.next()
            ACTION_PREVIOUS -> playerInteractor!!.previous()
            ACTION_TOGGLE_REPEAT_MODE -> playerInteractor!!.toggleRepeatMode()
            ACTION_TOGGLE_SHUFFLE_MODE -> playerInteractor!!.toggleShuffleMode()
            ACTION_START_FOREGROUND -> startForeground(notificationId, notification)
            ACTION_STOP_FOREGROUND -> stopForeground(false)
            ACTION_STOP_SERVICE -> {
                stopForeground(true)
                stopSelf()
            }
            else -> throw IllegalArgumentException(action)
        }
    }

    override fun onDestroy() {
        playerInteractor!!.setNewtonePlayerListener(null)
    }

    private val binder = LocalBinder()

    private inner class LocalBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    companion object {
        const val ACTION_TOGGLE_PLAYING = "com.parabola.newtone.TOGGLE_PLAYING"
        const val ACTION_NEXT = "com.parabola.newtone.NEXT"
        const val ACTION_PREVIOUS = "com.parabola.newtone.PREVIOUS"

        const val ACTION_TOGGLE_REPEAT_MODE = "com.parabola.newtone.TOGGLE_REPEAT_MODE"
        const val ACTION_TOGGLE_SHUFFLE_MODE = "com.parabola.newtone.TOGGLE_SHUFFLE_MODE"


        private const val ACTION_START_FOREGROUND = "com.parabola.newtone.START_FOREGROUND"
        private const val ACTION_STOP_FOREGROUND = "com.parabola.newtone.STOP_FOREGROUND"
        private const val ACTION_STOP_SERVICE = "com.parabola.newtone.STOP_SERVICE"


        @SuppressLint("StaticFieldLeak")
        var playerInteractor: PlayerInteractorImpl? = null
    }
}
