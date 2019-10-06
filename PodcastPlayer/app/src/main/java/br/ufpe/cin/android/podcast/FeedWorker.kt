package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class FeedWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            Intent(
                ACTION_RELOAD_FEED
            )
        )

        return Result.success()
    }
}
