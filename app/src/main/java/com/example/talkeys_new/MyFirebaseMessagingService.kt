package com.example.talkeys_new

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.talkeys_new.utils.FCMTokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        // Create notification channel early to ensure proper permission handling
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here
        Log.d(TAG, "FCM message received")

        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }

        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        Log.d(TAG, "FCM token event at $timestamp")

        val previousToken = FCMTokenManager.getStoredToken(this)
        when {
            previousToken == null -> Log.d(TAG, "Token generated (first time)")
            previousToken != token -> Log.d(TAG, "Token refreshed")
            else -> Log.d(TAG, "Token unchanged")
        }

        FCMTokenManager.storeToken(this, token)
        storeTokenMetadata(token, timestamp)
        sendRegistrationToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Handle data payload of FCM messages here
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server
        // This should make an API call to your backend to update the token
        // for the current user
        Log.d(TAG, "TODO: send FCM token to backend (not yet implemented)")

        // Add this through shared Ktor once the backend exposes an FCM-token
        // registration endpoint.
    }
    
    private fun storeTokenMetadata(token: String, timestamp: String) {
        val sharedPref = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("fcm_token_timestamp", timestamp)
            putInt("fcm_token_generation_count", getTokenGenerationCount() + 1)
            apply()
        }
        Log.d(TAG, "📊 Token metadata stored - Generation #${getTokenGenerationCount()}")
    }
    
    private fun getTokenGenerationCount(): Int {
        val sharedPref = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("fcm_token_generation_count", 0)
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channel = NotificationChannel(
                channelId,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifications from Talkeys app"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
