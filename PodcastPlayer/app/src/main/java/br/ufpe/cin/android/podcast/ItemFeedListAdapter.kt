package br.ufpe.cin.android.podcast

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.db.ItemFeed
import br.ufpe.cin.android.podcast.helpers.DateHelper
import org.jetbrains.anko.sdk27.coroutines.onClick

class ItemFeedListAdapter :
/*Paged*/ListAdapter<ItemFeed, ItemFeedListAdapter.ItemFeedViewHolder>(DIFF_CALLBACK) {

    class ItemFeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.item_title)
        private val date: TextView = view.findViewById(R.id.item_date)
        private val action: ImageView = view.findViewById(R.id.item_action)

        fun bindTo(item: ItemFeed) {
            title.apply {
                text = item.title

                onClick {
                    // Starts the EpisodeDetail activity
                    Intent(context, EpisodeDetailActivity::class.java).apply {
                        // Passes the ItemFeed uid to the EpisodeDetail activity
                        putExtra("item_uid", item.uid)
                    }.also { context.startActivity(it) }
                }
            }

            date.apply {
                text = DateHelper.parseToString(context, item.pubDate)
            }

            action.apply {
                val icon = if (item.fileLocation == null) {
                    R.drawable.ic_file_download_grey_900_24dp
                } else {
                    R.drawable.ic_play_arrow_grey_900_24dp
                }

                setImageResource(icon)

                onClick {
                    if (item.fileLocation == null) {
                        Intent(context, DownloadService::class.java).apply {
                            putExtra("item_uid", item.uid)
                        }.also { context.startService(it) }
                    } else {
                        (context as MainActivity).playerService.load(item)
                        (context as MainActivity).playerService.play()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemFeedViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.itemlista,
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ItemFeedViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bindTo(item)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ItemFeed>() {

            override fun areItemsTheSame(oldItem: ItemFeed, newItem: ItemFeed) =
                oldItem.uid == newItem.uid

            override fun areContentsTheSame(oldItem: ItemFeed, newItem: ItemFeed) =
                oldItem.fileLocation == newItem.fileLocation && oldItem.currentLength == newItem.currentLength
        }
    }

}
