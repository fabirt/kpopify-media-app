package com.fabirt.kpopify.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.fabirt.kpopify.core.constants.K

class DummyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(
            context,
            "Pretending to do something dummy",
            Toast.LENGTH_LONG
        ).show()

        val notificationId = intent!!.getIntExtra(K.EXTRA_ACTION_TEST, -1)
        if (notificationId > 0) {
            NotificationManagerCompat.from(context!!).apply {
                cancel(notificationId)
            }
        }
    }
}