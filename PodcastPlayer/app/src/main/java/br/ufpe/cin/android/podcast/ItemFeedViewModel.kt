package br.ufpe.cin.android.podcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import br.ufpe.cin.android.podcast.db.AppDatabase
import br.ufpe.cin.android.podcast.db.ItemFeed

class ItemFeedViewModel(application: Application): AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    internal val allItems = db.itemFeedDAO().getAllSorted()

    fun saveItem(itemFeed: ItemFeed) {
        db.itemFeedDAO().insertAll(itemFeed)
    }
}