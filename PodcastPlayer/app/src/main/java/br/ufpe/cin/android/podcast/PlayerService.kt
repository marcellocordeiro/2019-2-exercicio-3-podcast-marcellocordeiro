package br.ufpe.cin.android.podcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File


class PlayerService : Service() {

    val ACTION_START_PLAYER_SERVICE = "ACTION_START_PLAYER_SERVICE"
    val ACTION_STOP_PLAYER_SERVICE = "ACTION_STOP_PLAYER_SERVICE"
    val ACTION_PAUSE = "ACTION_PAUSE"
    val ACTION_PLAY = "ACTION_PLAY"

    private val CHANNEL_ID = "ForegroundServiceChannel"
    private val playerBinder = PlayerBinder()

    var musicPlayer = MediaPlayer()
    var isLoaded = false

    inner class PlayerBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_pause_grey_900_24dp)
            setContentTitle("Foreground Service")
            setContentText("Hello")
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingIntent)
        }.build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Do heavy work on a background thread
        // stopSelf();

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return playerBinder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Foreground Service Channel"
            val descriptionText = "Playback service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun load(string: String) {
        musicPlayer.reset()
        musicPlayer.setDataSource(applicationContext, Uri.fromFile(File(string)))
        musicPlayer.prepare()
        isLoaded = true
    }

    fun toggle() {
        if (!isLoaded) {
            return
        }

        if (musicPlayer.isPlaying) {
            musicPlayer.pause()
        } else {
            musicPlayer.start()

            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                stopForeground(STOP_FOREGROUND_DETACH)
            else
                stopForeground(false)*/
        }
    }
}