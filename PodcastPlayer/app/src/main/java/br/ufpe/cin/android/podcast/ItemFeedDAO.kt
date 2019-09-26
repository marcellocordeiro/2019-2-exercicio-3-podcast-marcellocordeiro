package br.ufpe.cin.android.podcast

import androidx.room.*

@Dao
interface ItemFeedDAO {

    @Query("SELECT * FROM ItemFeed")
    fun getAll(): List<ItemFeed>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg entries: ItemFeed)

    @Delete
    fun delete(entry: ItemFeed)
}
