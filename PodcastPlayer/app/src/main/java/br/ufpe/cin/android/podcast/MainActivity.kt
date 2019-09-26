package br.ufpe.cin.android.podcast

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
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

        setPodcastFeed(rssLink)
    }

    private fun setPodcastFeed(rssLink: String) {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        val db = AppDatabase.getInstance(this)

        doAsync {
            try {
                // Downloads the list of episodes, the feed title and the feed banner
                val (feedData, title, img) = if (isConnected()) {
                    val rss = URL(rssLink).readText()
                    val (title, imageLink) = Parser.parseInfo(rss)

                    val img = if (imageLink != null) {
                        Picasso.get().load(imageLink)
                    } else {
                        null
                    }

                    // Saves old title and image link
                    val editor = preferences.edit()

                    editor.putString("title", title ?: "")
                    editor.putString("imageLink", imageLink ?: "")
                    editor.apply()

                    Triple(Parser.parse(rss), title, img)
                } else {
                    val oldFeed = db.itemFeedDAO().getAll()

                    if (oldFeed.isEmpty()) {
                        showSnackbar(
                            window.decorView.rootView,
                            getString(R.string.no_cached_database_found)
                        )
                    } else {
                        showSnackbar(
                            window.decorView.rootView,
                            getString(R.string.using_cached_db_warning)
                        )
                    }

                    // Gets previously saved title and image link
                    val title = preferences.getString("title", null)
                    val imageLink = preferences.getString("imageLink", null)

                    // Even if offline, the image should have been cached
                    val img = if (imageLink != null) {
                        Picasso.get().load(imageLink)
                    } else {
                        null
                    }

                    // If something goes wrong, returns the existent feed list
                    Triple(oldFeed, title, img)
                }

                // TODO: do this more efficiently
                db.itemFeedDAO().insertAll(*feedData.toTypedArray())

                uiThread {
                    refreshFeed(it, feedData, title, img)
                }
            } catch (e: Exception) {
                uiThread {
                    progressBar.visibility = View.GONE

                    showSnackbar(
                        window.decorView.rootView,
                        e.message ?: e.toString()
                    )
                }
            }
        }
    }

    private fun refreshFeed(context: Context, feedData: List<ItemFeed>, title: String?, banner: RequestCreator?) {
        progressBar.visibility = View.GONE

        // Shows the feed
        feedView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = MyAdapter(feedData)

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }

        // If there is a banner, sets it to the ImageView
        if (banner != null) {
            banner.into(app_bar_image)
            app_bar.setExpanded(false, false)
            app_bar_image.visibility = View.VISIBLE
            app_bar.setExpanded(true, true)
        } else {
            // TODO: fix this
            // app_bar_image.visibility = View.GONE
        }

        // If there is a title, sets it to the toolbar
        if (title != null) {
            toolbar_layout.title = title
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
