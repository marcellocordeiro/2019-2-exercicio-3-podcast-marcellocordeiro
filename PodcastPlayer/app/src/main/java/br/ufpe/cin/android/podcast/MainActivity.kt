package br.ufpe.cin.android.podcast

import android.content.*
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.db.AppDatabase
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_feed.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var playerService: PlayerService
    private var playerIsBound = false

    private val receiver = Receiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Intent(applicationContext, PlayerService::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
            } else {
                startService(it)
            }
        }

        floatingActionButton.onClick {
            Intent(applicationContext, SettingsActivity::class.java).also {
                startActivity(it)
            }
        }

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(receiver, receiver.intentFiter)

        // Hides the CollapsingToolbar
        //app_bar.setExpanded(false, false)
        toolbar_layout.title = getString(R.string.app_name)
        toolbar_layout.setExpandedTitleColor(0)

        showFeed()
        showElements()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val feedLink =
            sharedPreferences.getString("feed_link", "null") ?: "null"

        if (feedLink == "null") {
            sharedPreferences.edit().putString("feed_link", "https://ffkhunion.libsyn.com/rss")
                .apply()
            updateFeed()
        }
    }

    private fun showElements() {
        progress_bar.visibility = View.GONE
        feedView.visibility = View.VISIBLE
    }

    private fun showFeed() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        feedView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ItemFeedListAdapter()
            //setHasFixedSize(true)

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }

        val model = ViewModelProviders.of(this).get(ItemFeedViewModel::class.java)
        model.itemList.observe(
            this,
            Observer { (feedView.adapter as ItemFeedListAdapter).submitList(it) })

        val title = sharedPreferences.getString("cached_title", "unknown") ?: "unknown"
        val imageLink = sharedPreferences.getString("cached_image_link", "") ?: ""

        toolbar_layout.title = title
        if (imageLink != "") {
            val img = Picasso.get().load(imageLink)

            img.into(toolbar_image)
            app_bar.setExpanded(false, false)
            toolbar_image.visibility = View.VISIBLE
            app_bar.setExpanded(true, true)
        }
    }

    fun updateFeed() {
        val db = AppDatabase.getInstance(this)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val feedLink =
            sharedPreferences.getString("feed_link", "null") ?: "null"

        doAsync {
            if (isConnected()) {
                val rss = URL(feedLink).readText()
                val newFeed = Parser.parse(rss)

                db.itemFeedDAO().insertAll(*newFeed.toTypedArray())

                val (title, imageLink) = Parser.parseInfo(rss)

                if (title != null) {
                    toolbar_layout.title = title
                }

                if (imageLink != null) {
                    val img = Picasso.get().load(imageLink)

                    uiThread {
                        img.into(toolbar_image)
                        app_bar.setExpanded(false, false)
                        toolbar_image.visibility = View.VISIBLE
                        app_bar.setExpanded(true, true)
                    }
                } else {
                    // TODO: fix this
                    // app_bar_image.visibility = View.GONE
                }

                sharedPreferences.edit().apply {
                    putString("cached_title", title ?: "unknown")
                    putString("cached_image_link", imageLink ?: "")
                }.apply()
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

    override fun onStart() {
        super.onStart()

        Intent(applicationContext, PlayerService::class.java).also { intent ->
            //intent.action = playerService.ACTION_START_PLAYER_SERVICE
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()

        unbindService(connection)
        playerIsBound = false
    }

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


    inner class Receiver : BroadcastReceiver() {

        val intentFiter: IntentFilter
            get() = IntentFilter().apply {
                addAction(ACTION_RELOAD_FEED)
            }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action ?: return) {
                ACTION_RELOAD_FEED -> updateFeed()
            }

        }
    }
}
