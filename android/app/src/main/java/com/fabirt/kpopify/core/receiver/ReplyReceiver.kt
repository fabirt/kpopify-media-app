package com.fabirt.kpopify.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.fabirt.kpopify.core.constant.K

class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        getMessageText(intent!!)?.let { message ->
            Toast.makeText(
                context,
                "Pretending to do something useful with the message: $message",
                Toast.LENGTH_LONG
            ).show()

            val notificationId = intent.getIntExtra(K.EXTRA_ACTION_TEST, -1)
            if (notificationId > 0) {
                NotificationManagerCompat.from(context!!).apply {
                    cancel(notificationId)
                }
            }
        }
    }

    private fun getMessageText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent).getCharSequence(K.KEY_TEXT_REPLY)
    }
}