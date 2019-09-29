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

class ItemFeedListAdapter(private var myDataset: List<ItemFeed> = emptyList()) :
    RecyclerView.Adapter<ItemFeedListAdapter.MyViewHolder>() {

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    fun setDataset(newDataset: List<ItemFeed>) {
        myDataset = newDataset
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemlista, parent, false) as View
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.apply {
            val isPresentLocally = myDataset[position].fileLocation != null

            item_title.apply {
                //text = myDataset[position].fileLocation ?: "nope"
                text = myDataset[position].title

                // Starts the EpisodeDetail activity
                onClick {
                    val i = Intent(context, EpisodeDetailActivity::class.java).apply {
                        // Passes the ItemFeed uid to the EpisodeDetail activity
                        putExtra("item_uid", myDataset[position].uid)
                    }

                    startActivity(context, i, null)
                }
            }

            item_date.apply {
                text = DateHelper.parseToString(context, myDataset[position].pubDate)
            }

            item_action.apply {
                if (isPresentLocally) {
                    this.setImageResource(R.drawable.ic_play_arrow_grey_900_24dp)
                }
                // Opens the episode's download link
                onClick {
                    val intent = Intent(context, DownloadService::class.java)
                    intent.putExtra("item_uid", myDataset[position].uid)
                    context.startService(intent)
                }
            }
        }
    }

    override fun getItemCount() = myDataset.size
}
