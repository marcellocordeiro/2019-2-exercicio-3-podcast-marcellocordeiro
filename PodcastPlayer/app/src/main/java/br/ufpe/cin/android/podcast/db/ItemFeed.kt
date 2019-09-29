package br.ufpe.cin.android.podcast.db

import androidx.room.*

@Entity(indices = [Index(value = ["title"], unique = true)])
data class ItemFeed(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "link") val link: String,
    @ColumnInfo(name = "length") val length: Int,
    @ColumnInfo(name = "pubDate") val pubDate: Long,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "imageLink") val imageLink: String,
    @ColumnInfo(name = "downloadLink") val downloadLink: String
) {

    @PrimaryKey(autoGenerate = true) var uid: Int = 0

    @ColumnInfo var fileLocation: String? = null
    @ColumnInfo var currentLength: Int = 0

    override fun toString(): String {
        return title
    }
}
