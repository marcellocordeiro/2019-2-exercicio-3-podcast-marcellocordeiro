package br.ufpe.cin.android.podcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import br.ufpe.cin.android.podcast.db.AppDatabase

class ItemFeedViewModel(application: Application) : AndroidViewModel(application) {

    val itemList = AppDatabase.getInstance(application).itemFeedDAO().getAllSorted()
}
