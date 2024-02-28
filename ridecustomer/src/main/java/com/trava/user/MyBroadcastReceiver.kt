package com.trava.user

import android.app.slice.Slice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra(Slice.EXTRA_TOGGLE_STATE)) {
            Toast.makeText(context, "Toggled:  " + intent.getBooleanExtra(
                    Slice.EXTRA_TOGGLE_STATE, false),
                    Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_MESSAGE = "message"
    }
}