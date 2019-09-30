package br.ufpe.cin.android.podcast

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock.sleep
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.db.AppDatabase
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_feed.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var playerService: PlayerService
    private var playerIsBound = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PlayerService.PlayerBinder
            playerService = binder.service
            playerIsBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            playerIsBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerServiceIntent = Intent(this, PlayerService::class.java)
        //startService(playerServiceIntent)
        ContextCompat.startForegroundService(this, playerServiceIntent)

        // Hides the CollapsingToolbar
        app_bar.setExpanded(false, false)
        toolbar_layout.title = getString(R.string.app_name)
        toolbar_layout.setExpandedTitleColor(0)

        // Hardcoded download links for the feed
        val rssLink = "https://ffkhunion.libsyn.com/rss"

        feedView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ItemFeedListAdapter()

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }

        setPodcastFeed(rssLink)
    }

    private fun setPodcastFeed(rssLink: String) {
        val db = AppDatabase.getInstance(this)

        doAsync {
            if (isConnected()) {
                val rss = URL(rssLink).readText()
                val newFeed = Parser.parse(rss)

                db.itemFeedDAO().insertAll(*newFeed.toTypedArray())

                val (title, imageLink) = Parser.parseInfo(rss)

                if (title != null) {
                    toolbar_layout.title = title
                }

                if (imageLink != null) {
                    val img = Picasso.get().load(imageLink)

                    uiThread {
                        img.into(app_bar_image)
                        app_bar.setExpanded(false, false)
                        app_bar_image.visibility = View.VISIBLE
                    }
                } else {
                    // TODO: fix this
                    // app_bar_image.visibility = View.GONE
                }
            }

            val currentFeedData = db.itemFeedDAO().getAllSorted()

            uiThread {
                (feedView.adapter as ItemFeedListAdapter).setDataset(currentFeedData)

                onButton.setOnClickListener {
                    if (!playerService.isLoaded) {
                        playerService.load(currentFeedData[0].fileLocation!!)
                    } else {
                        playerService.toggle()
                    }
                }

                if (app_bar_image.visibility == View.VISIBLE) {
                    app_bar.setExpanded(true, true)
                }

                progressBar.visibility = View.GONE
            }
        }
    }

    // Checks if the device is connected to a network,
    // but doesn't check for an active internet connection
    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork

        return activeNetwork != null
    }

    private fun showSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(view, message, duration).show()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()

        Toast.makeText(this, "Fazendo o Binding...", Toast.LENGTH_SHORT).show()
        Intent(this, PlayerService::class.java).also { intent ->
            //intent.action = playerService.ACTION_START_PLAYER_SERVICE
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        //val serviceIntent = Intent(this, PlayerService::class.java)
        //stopService(serviceIntent)
        Toast.makeText(applicationContext, "Desfazendo o Binding...", Toast.LENGTH_SHORT).show()
        unbindService(connection)
        playerIsBound = false
    }
}
