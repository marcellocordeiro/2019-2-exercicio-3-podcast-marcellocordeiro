package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class Receiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        // val extra = intent.extras

        when (context) {
            is PlayerService -> {
                when (action) {
                    ACTION_TOGGLE -> context.toggle()
                }
            }
        }
    }
}