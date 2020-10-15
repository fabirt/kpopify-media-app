package com.fabirt.kpopify.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.fabirt.kpopify.core.constants.K
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KpopifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createTestNotificationChannel()
    }

    private fun createTestNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifications"
            val descriptionText = "App messages"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(K.TEST_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            NotificationManagerCompat.from(applicationContext).createNotificationChannel(channel)
        }
    }
}
