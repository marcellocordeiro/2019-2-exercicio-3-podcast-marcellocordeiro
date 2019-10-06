package br.ufpe.cin.android.podcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.ufpe.cin.android.podcast.db.AppDatabase
import br.ufpe.cin.android.podcast.db.ItemFeed
import br.ufpe.cin.android.podcast.db.ItemFeedDAO
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class PlayerService : Service() {

    private lateinit var itemFeedDAO: ItemFeedDAO

    private val playerBinder = PlayerBinder()
    private val receiver = Receiver()

    private var player = MediaPlayer()
    private var isLoaded = false
    private var currentItem: ItemFeed? = null

    inner class PlayerBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    override fun onCreate() {
        super.onCreate()

        itemFeedDAO = AppDatabase.getInstance(applicationContext).itemFeedDAO()

        player.setOnCompletionListener {
            File(currentItem!!.fileLocation!!).delete()

            doAsync {
                itemFeedDAO.updateFileLocationById(currentItem!!.uid, null)

                uiThread { reset() }
            }
        }

        createNotificationChannel()
        createNotification(
            "No podcast has been loaded",
            "Select a podcast",
            R.drawable.ic_stop_grey_900_24dp
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
        unregisterReceiver(receiver)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return playerBinder
    }

    private fun createNotification(title: String, description: String, smallIcon: Int) {
        registerReceiver(receiver, receiver.intentFiter)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val toggleIntent = Intent().apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent =
            PendingIntent.getBroadcast(applicationContext, 0, toggleIntent, 0)

        val stopIntent = Intent().apply {
            action = ACTION_STOP_PLAYER
        }
        val stopPendingIntent =
            PendingIntent.getBroadcast(applicationContext, 0, stopIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(smallIcon)
            setContentTitle(title)
            setContentText(description)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingIntent)
            addAction(R.drawable.ic_pause_grey_900_24dp, "Toggle", togglePendingIntent)
            addAction(R.drawable.ic_stop_grey_900_24dp, "Close", stopPendingIntent)
        }.build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Player Service Channel"
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

    fun load(item: ItemFeed) {
        player.reset()
        player.setDataSource(
            applicationContext,
            Uri.fromFile(File(item.fileLocation!!))
        )
        player.prepare()
        player.seekTo(item.currentLength)

        currentItem = item
        isLoaded = true
    }

    fun play() {
        createNotification(
            currentItem?.title ?: "null",
            "Playing...",
            R.drawable.ic_play_arrow_grey_900_24dp
        )
        player.start()
    }

    fun pause() {
        createNotification(
            currentItem?.title ?: "null",
            "Paused",
            R.drawable.ic_pause_grey_900_24dp
        )
        player.pause()

        val currentTime = player.currentPosition

        doAsync { itemFeedDAO.updateCurrentLengthById(currentItem!!.uid, currentTime) }
    }

    fun stop() {
        if (currentItem != null) {
            player.pause()
            val currentTime = player.currentPosition

            doAsync {
                itemFeedDAO.updateCurrentLengthById(currentItem!!.uid, currentTime)

                uiThread {
                    stopSelf()
                }
            }
        } else {
            stopSelf()
        }
    }

    fun toggle() {
        if (!isLoaded) {
            return
        }

        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun reset() {
        currentItem = null
        isLoaded = false

        createNotification(
            "No podcast has been loaded",
            "Select a podcast",
            R.drawable.ic_stop_grey_900_24dp
        )
    }

    inner class Receiver : BroadcastReceiver() {

        val intentFiter: IntentFilter
            get() = IntentFilter().apply {
                addAction(ACTION_TOGGLE)
                addAction(ACTION_STOP_PLAYER)
            }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action ?: return) {
                ACTION_TOGGLE -> toggle()
                ACTION_STOP_PLAYER -> stop()
            }

        }
    }

    companion object {
        const val CHANNEL_ID = "PlayerServiceChannel"
    }
}
