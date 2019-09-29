package br.ufpe.cin.android.podcast

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hides the CollapsingToolbar
        app_bar.setExpanded(false, false)
        toolbar_layout.title = getString(R.string.app_name)
        toolbar_layout.setExpandedTitleColor(0)

        // Hardcoded download links for the feed
        val rssLink = "https://ffkhunion.libsyn.com/rss"

        feedView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ItemFeedListAdapter()
            itemAnimator = DefaultItemAnimator()

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }

        val model = ViewModelProviders.of(this).get(ItemFeedViewModel::class.java)
        model.allItems.observe(
            this,
            Observer { (feedView.adapter as ItemFeedListAdapter).setDataset(it) })

        setPodcastFeed(rssLink)
    }

    private fun setPodcastFeed(rssLink: String) {
        val db = AppDatabase.getInstance(this)

        doAsync {
            val currentFeedData = db.itemFeedDAO().getAllSorted()

            if (isConnected()) {
                val rss = URL(rssLink).readText()
                val newFeed = Parser.parse(rss)
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
                        app_bar.setExpanded(true, true)
                    }
                } else {
                    // TODO: fix this
                    // app_bar_image.visibility = View.GONE
                }

                db.itemFeedDAO().insertAll(*newFeed.toTypedArray())

            }

            uiThread {
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
}
