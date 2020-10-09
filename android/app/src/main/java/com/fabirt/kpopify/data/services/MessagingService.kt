package com.fabirt.kpopify.data.services

import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "MessagingService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.title?.let { Log.i(TAG, it) }
    }

    override fun onNewToken(token: String) {
        // Get the clipboard system service
        val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
        val clip = ClipData.newPlainText("RANDOM_UUID", token)
        clipboard?.setPrimaryClip(clip)
        Log.i(TAG,"Token copied!")
    }
}