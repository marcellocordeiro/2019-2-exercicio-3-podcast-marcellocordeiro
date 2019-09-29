package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.ufpe.cin.android.podcast.db.AppDatabase
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService("DownloadHelper") {

    public override fun onHandleIntent(i: Intent?) {
        if (i == null) {
            Log.e("NULL_INTENT", "Intent is null")
            return
        }

        val itemUid = i.getIntExtra("item_uid", -1)

        if (itemUid == -1) {
            Log.e("NULL_UID", "Item UID is null")
            return
        }

        val db = AppDatabase.getInstance(this)
        val item = db.itemFeedDAO().getById(itemUid)

        if (item == null) {
            Log.e("NULL_ITEM", "Item is null")
            return
        }

        val uri = Uri.parse(item.downloadLink)
        val url = URL(item.downloadLink)
        val outputFile = uri.lastPathSegment ?: "unknown"

        val root = getExternalFilesDir(DIRECTORY_DOWNLOADS)
        root?.mkdirs()
        val output = File(root, outputFile)
        if (output.exists()) {
            output.delete()
        }

        try {
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len = `in`.read(buffer)
                while (len >= 0) {
                    out.write(buffer, 0, len)
                    len = `in`.read(buffer)
                }
                out.flush()
            } finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

            db.itemFeedDAO().updateFileLocationById(itemUid, output.absolutePath)
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(DOWNLOAD_COMPLETE))
        } catch (e2: IOException) {
            Log.e(javaClass.name, "Exception durante download", e2)
        }

    }

    companion object {
        const val DOWNLOAD_COMPLETE =
            "br.ufpe.cin.android.podcast.action.DOWNLOAD_COMPLETE"
    }
}