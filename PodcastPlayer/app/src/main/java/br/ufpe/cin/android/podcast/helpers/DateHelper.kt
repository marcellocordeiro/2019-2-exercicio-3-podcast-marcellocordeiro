package br.ufpe.cin.android.podcast.helpers

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    private const val feedFormat = "EEE, d MMM yyyy HH:mm:ss Z"
    private val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

    //val formatter = DateTimeFormatter.ofPattern(feedFormat )


    fun parseToLong(string: String): Long {
        return formatter.parse(string)?.time ?: Date(0).time
    }

    fun parseToString(context: Context, long: Long): String {
        val date = Date(long)
        val dateFormat = android.text.format.DateFormat.getDateFormat(context) as SimpleDateFormat
        val pattern = dateFormat
        return pattern.format(date)
    }


}