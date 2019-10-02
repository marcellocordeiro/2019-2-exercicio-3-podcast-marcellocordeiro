package br.ufpe.cin.android.podcast

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.db.ItemFeed
import br.ufpe.cin.android.podcast.helpers.DateHelper
import kotlinx.android.synthetic.main.itemlista.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class ItemFeedListAdapterBackup(private var myDataset: List<ItemFeed> = emptyList()) :
    RecyclerView.Adapter<ItemFeedListAdapterBackup.ItemFeedViewHolderBackup>() {

    class ItemFeedViewHolderBackup(val view: View) : RecyclerView.ViewHolder(view)

    fun setDataset(newDataset: List<ItemFeed>) {
        myDataset = newDataset
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemFeedViewHolderBackup {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemlista, parent, false) as View
        return ItemFeedViewHolderBackup(view)
    }

    override fun onBindViewHolder(holder: ItemFeedViewHolderBackup, position: Int) {
        val currentItem = myDataset[position]

        holder.view.apply {
            item_title.apply {
                text = currentItem.title

                // Starts the EpisodeDetail activity
                onClick {
                    val i = Intent(context, EpisodeDetailActivity::class.java).apply {
                        // Passes the ItemFeed uid to the EpisodeDetail activity
                        putExtra("item_uid", currentItem.uid)
                    }

                    startActivity(context, i, null)
                }
            }

            item_date.apply {
                text = DateHelper.parseToString(context, currentItem.pubDate)
            }

            item_action.apply {
                val fileLocation = currentItem.fileLocation

                if (fileLocation != null) {
                    this.setImageResource(R.drawable.ic_play_arrow_grey_900_24dp)
                }

                // Opens the episode's download link
                onClick {
                    if (fileLocation != null) {
                        // Play
                    } else {
                        val intent = Intent(context, DownloadService::class.java)
                        intent.putExtra("item_uid", currentItem.uid)
                        context.startService(intent)
                    }
                }
            }
        }
    }

    override fun getItemCount() = myDataset.size
}
