package br.ufpe.cin.android.podcast

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import br.ufpe.cin.android.podcast.db.AppDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_episode_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread

class EpisodeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)

        // Takes the ItemFeed object passed by the main activity
        val itemUid = intent.getIntExtra("item_uid", -1)

        if (itemUid == -1) {
            finish()
        }

        val db = AppDatabase.getInstance(this)

        doAsync {
            val item = db.itemFeedDAO().getById(itemUid)!!
            val episodeImage = Picasso.get().load(item.imageLink)

            uiThread {

                episodeImage.into(episode_image)
                episode_image.visibility = View.VISIBLE

                episode_title.text = item.title

                // Formats the episode's description
                episode_description.text =
                    HtmlCompat.fromHtml(item.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        .trim()

                episode_link.apply {
                    // Opens the episode's link
                    onClick {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(item.link)
                        startActivity(i)
                    }
                }
            }
        }
    }
}
