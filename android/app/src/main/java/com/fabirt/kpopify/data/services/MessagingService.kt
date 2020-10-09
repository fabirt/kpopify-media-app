package com.fabirt.kpopify.data.services

import android.app.Notification
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.fabirt.kpopify.MainActivity
import com.fabirt.kpopify.R
import com.fabirt.kpopify.core.constants.K.NOTIFICATION_CHANNEL_ID
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "MessagingService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.i(TAG, remoteMessage.toString())
        // Handle notification
        val notification = buildNotification("Hello")
        NotificationManagerCompat.from(applicationContext).notify(2000, notification)
    }

    override fun onNewToken(token: String) {
        // Get the clipboard system service
        val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
        val clip = ClipData.newPlainText("RANDOM_UUID", token)
        clipboard?.setPrimaryClip(clip)
        Log.i(TAG, "Token copied!")
    }

    private fun buildNotification(content: String): Notification {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            0
        )

        val accentColor = ContextCompat.getColor(this, R.color.colorAccent)

        val dummyAction = NotificationCompat.Action.Builder(
            R.drawable.common_google_signin_btn_icon_dark,
            "Dummy button",
            pendingIntent
        ).build()

        val remoteInput = RemoteInput.Builder("KEY_TEXT_REPLY")
            .setLabel("Message")
            .build()

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.common_google_signin_btn_icon_dark,
            "Reply",
            pendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(accentColor)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.mipmap.ic_launcher
                )
            )
            .setContentTitle("Kpopify")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(replyAction)
            .addAction(dummyAction)

        return notification.build()
    }
}