package br.ufpe.cin.android.podcast

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import br.ufpe.cin.android.podcast.db.AppDatabase
import br.ufpe.cin.android.podcast.db.ItemFeedDAO
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var itemFeedDAO: ItemFeedDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        itemFeedDAO = AppDatabase.getInstance(applicationContext).itemFeedDAO()
    }

    override fun onResume() {
        super.onResume()

        getDefaultSharedPreferences(applicationContext).registerOnSharedPreferenceChangeListener(
            this
        )
    }

    override fun onStop() {
        super.onStop()

        getDefaultSharedPreferences(applicationContext).unregisterOnSharedPreferenceChangeListener(
            this
        )
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "feed_link") {
            doAsync {
                itemFeedDAO.deleteAll()

                uiThread {
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                        Intent(
                            ACTION_RELOAD_FEED
                        )
                    )
                }
            }
        }

        if (key == "sync_master_toggle") {
            val enabled = sharedPreferences?.getBoolean("sync_master_toggle", false) ?: false

            if (enabled) {
                val period = sharedPreferences?.getString("sync_period", "1")?.toLong() ?: 1

                val updateFeedWorkRequest =
                    PeriodicWorkRequest.Builder(FeedWorker::class.java, period, TimeUnit.HOURS).build()
                WorkManager.getInstance(applicationContext).also {
                    it.cancelAllWork()
                    it.enqueue(updateFeedWorkRequest)
                }
            } else {
                WorkManager.getInstance(applicationContext).also {
                    it.cancelAllWork()
                }
            }
        }
    }
}
