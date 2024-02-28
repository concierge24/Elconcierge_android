package com.trava.user.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trava.user.MainActivity
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.utilities.Broadcast
import com.trava.utilities.Logger
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.FCM_TOKEN
import org.json.JSONObject

class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = AppFirebaseMessagingService::class.java.simpleName
    private var intent: Intent? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage?.data?.isNotEmpty() == true) {
            Logger.e(tag, "Message data payload: " + remoteMessage.data["message"])

            val intent = Intent()
            intent.action = Broadcast.NOTIFICATION
            intent.putExtra("order_id", remoteMessage.data["order_id"])
            intent.putExtra("type", remoteMessage.data["type"])
            intent.putExtra("message", remoteMessage.data["message"])
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
        sendNotification(remoteMessage.notification?.body ?: "",remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SharedPrefs.with(applicationContext).save(FCM_TOKEN, token)
    }

    private fun sendNotification(messageBody: String,remoteMessage: RemoteMessage) {
        var data=""
        if (remoteMessage.data.get("data")!=null)
        {
            val type = JSONObject(remoteMessage.data.get("data"))
            data= type.getString("type")
        }
        when {
            data == "wallet" -> {
                intent = Intent(applicationContext,MainActivity::class.java)
                intent?.putExtra("order_id", remoteMessage.data["order_id"])
                intent?.putExtra("type", data)
                intent?.putExtra("notification", "notification")
                intent?.putExtra("message", remoteMessage.data["message"])
            }
            remoteMessage.data["type"]=="chat" -> {
                intent = Intent(applicationContext,MainActivity::class.java)
                intent?.putExtra("order_id", remoteMessage.data["order_id"])
                intent?.putExtra("type", remoteMessage.data["type"])
                intent?.putExtra("notification", "notification")
                intent?.putExtra("message", remoteMessage.data["message"])
            }
            else -> {
                intent = Intent(this, HomeActivity::class.java)
            }
        }
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setWhen(0)
//                .setSmallIcon(R.drawable.ic_gomove_notif)
                .setSmallIcon(R.drawable.ic_notif)
//                .setSmallIcon(R.drawable.ic_notif_q)
//                .setSmallIcon(R.drawable.transporter_splash_new)
                .setColor(applicationContext.resources.getColor(R.color.text_dark2))
                .setContentTitle(getString(R.string.app_name))
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}