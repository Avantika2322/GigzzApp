package com.gigzz.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.ui.home.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val CHANNEL_ID = "com.icourt.notification.channel_id"
    private val CHANNEL_NAME = "com.icourt.notification.channel_name"
    private lateinit var prefHelp: PreferenceDataStoreModule
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    companion object {
        var notiType: String? = null
        var pendingIntent: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        prefHelp = PreferenceDataStoreModule(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebase", "Token: $token")
        serviceScope.launch {
            prefHelp.putPreference(PrefKeys.DEVICE_TOKEN, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("MyFirebase","message: ${message.data}")
        if (message.data.isNotEmpty()) {
            sendNotification(
                message.data["title"].toString(),
                message.data["message"].toString(),
                message.data["notification_type"].toString()
            )
        }
    }

    private fun sendNotification(title: String, message: String, notificationType: String?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //intent.putExtra("notificationType", notificationType)
        //   intent.putExtra("pendingIntent", true)
        //  intent.putExtra("pendingIntent", false)
        notiType = notificationType
        pendingIntent = notiType != "8"


        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.logo)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true)
                .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val adminChannel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(adminChannel)
        notificationManager.notify(0, notificationBuilder)
    }

}