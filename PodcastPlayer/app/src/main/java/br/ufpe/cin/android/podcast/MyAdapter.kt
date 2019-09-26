package br.ufpe.cin.android.podcast

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.itemlista.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MyAdapter(private val myDataset: List<ItemFeed>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemlista, parent, false) as View
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.apply {
            item_title.apply {
                text = myDataset[position].title

                // Starts the EpisodeDetail activity
                onClick {
                    val i = Intent(context, EpisodeDetailActivity::class.java).apply {
                        // Passes the ItemFeed object to the EpisodeDetail activity
                        putExtra("item_details", myDataset[position])
                    }

                    startActivity(context, i, null)
                }
            }

            item_date.apply {
                text = myDataset[position].pubDate
            }

            item_action.apply {
                // Opens the episode's download link
                onClick {
                    try {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(myDataset[position].downloadLink)
                        startActivity(context, i, null)
                    } catch (e: Exception) {
                        Snackbar.make(
                            holder.view,
                            e.message ?: e.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun getItemCount() = myDataset.size
}
