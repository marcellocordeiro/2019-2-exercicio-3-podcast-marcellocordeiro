package br.ufpe.cin.android.podcast

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ItemFeed(
    @PrimaryKey val title: String,
    @ColumnInfo val link: String,
    @ColumnInfo val pubDate: String,
    @ColumnInfo val description: String,
    @ColumnInfo val imageLink: String,
    @ColumnInfo val downloadLink: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun toString(): String {
        return title
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(link)
        parcel.writeString(pubDate)
        parcel.writeString(description)
        parcel.writeString(imageLink)
        parcel.writeString(downloadLink)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemFeed> {
        override fun createFromParcel(parcel: Parcel): ItemFeed {
            return ItemFeed(parcel)
        }

        override fun newArray(size: Int): Array<ItemFeed?> {
            return arrayOfNulls(size)
        }
    }
}
