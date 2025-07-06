package com.cp.fishthebreak.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cp.fishthebreak.R
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.screens.activities.MainActivity
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.getNotificationIcon
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MessagingService"
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${message.from}")
        val model = NotificationModel()
        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            model.title = it.title ?: "Test Title"
            model.message = it.body ?: "Test Body"
//            sendNotification(NotificationModel(it.title?:"Test Title",it.body?:"Test Body","",""))
        }
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            try {
                val notificationType = message.data["notification_type"].toString()
                val body = Gson().fromJson(
                    message.data.toString(),
                    ChatNotificationDataModel::class.java
                )
                if (notificationType == "chat" && body.my_data != null) {
                    model.navigation = NavigationDirections.OpenGroup(body.my_data)
                } else if (notificationType == "invitation") {
                    model.navigation = NavigationDirections.OpenGroupList()
                }
            } catch (ex: Exception) {
                Log.e("noti_model_exp", ex.message.toString())
            }
        }
        sendNotification(model)
//        if (model.notification_type_id == 13){
//            sendNotificationWithAction(model)
//        }else {
//            sendNotification(model)
//        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun sendNotification(model: NotificationModel) {
        val myIntent = Intent(applicationContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        myIntent.putExtra("notificationData", model)
        val resultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                applicationContext,
                Random().nextInt(),
                myIntent,
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                applicationContext, Random().nextInt(), myIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notificationChannel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                Constants.channelName, Constants.channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = ""
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }

        // to display notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = mNotificationManager.getNotificationChannel(Constants.channelName)
            channel.canBypassDnd()
        }
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, Constants.channelName)
        notificationBuilder
            .setContentTitle(model.title)
            .setContentText(model.message)
            .setGroup(Constants.channelName)
            .setStyle(NotificationCompat.BigTextStyle())
            .setDefaults(Notification.DEFAULT_ALL)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(this.getNotificationIcon())
            .setColor(ContextCompat.getColor(this, R.color.color_primary))
            .setAutoCancel(true)
            .setGroupSummary(true)
            .setContentIntent(resultPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationChannel != null) {
                notificationBuilder.setChannelId(Constants.channelName)
                mNotificationManager.createNotificationChannel(notificationChannel)
            }
        }
        mNotificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}