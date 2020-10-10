package com.fabirt.kpopify.core.services

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
import com.fabirt.kpopify.core.constants.K
import com.fabirt.kpopify.core.constants.K.NOTIFICATION_CHANNEL_ID
import com.fabirt.kpopify.core.receivers.DummyReceiver
import com.fabirt.kpopify.core.receivers.ReplyReceiver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "MessagingService"
        private const val notificationId = 1000
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.i(TAG, remoteMessage.toString())
        // Handle notification
        val notification = buildNotification()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    override fun onNewToken(token: String) {
        // Get the clipboard system service
        val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
        val clip = ClipData.newPlainText("RANDOM_UUID", token)
        clipboard?.setPrimaryClip(clip)
        Log.i(TAG, "Token copied!")
    }

    private fun buildNotification(): Notification {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            0
        )

        val replyIntent = Intent(this, ReplyReceiver::class.java).apply {
            putExtra(K.EXTRA_ACTION_TEST, notificationId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val dummyIntent = Intent(this, DummyReceiver::class.java).apply {
            putExtra(K.EXTRA_ACTION_TEST, notificationId)
        }
        val dummyPendingIntent = PendingIntent.getBroadcast(
            this, 0, dummyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val accentColor = ContextCompat.getColor(this, R.color.colorAccent)

        val dummyAction = NotificationCompat.Action.Builder(
            R.drawable.common_google_signin_btn_icon_dark,
            "Dummy button",
            dummyPendingIntent
        ).build()

        val remoteInput = RemoteInput.Builder(K.KEY_TEXT_REPLY)
            .setLabel("Message")
            .build()

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.common_google_signin_btn_icon_dark,
            "Reply",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(accentColor)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.mipmap.ic_launcher
                )
            )
            .setContentTitle("Hello there!")
            .setContentText("This is a message to test notification actions")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(replyAction)
            .addAction(dummyAction)

        return notification.build()
    }
}