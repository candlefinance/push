package com.candlefinance.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.internal.ResourceUtils
import java.util.Locale


object NotificationUtils {

  private const val NOTIFICATION_ID = 123321

  fun createNotificationChannel(
    context: Context,
    channelId: String,
    channelName: String,
    channelDescription: String
  ) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelId,
        channelName,
        NotificationManager.IMPORTANCE_HIGH
      )
      channel.description = channelDescription
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun createDefaultChannelForFCM(context:Context){
   val defaultChannelId=  context.resources.getString(R.string.default_notification_channel_id)
    val defaultChannelName = context.resources.getString(R.string.default_notification_channel_name)
    this
      .createNotificationChannel(context, defaultChannelId, defaultChannelName,"Default channel created by RN Push module to All FCM notification")
  }
  fun sendNotification(context: Context, title: String, message: String) {
    val defaultChannelId=  context.resources.getString(R.string.default_notification_channel_id)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val builder = NotificationCompat.Builder(context, defaultChannelId)
      .setSmallIcon(getResourceIdByName("ic_default_notification","drawable"))
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    notificationManager.notify(NOTIFICATION_ID, builder.build())
  }

  @SuppressLint("DiscouragedApi")
  fun getResourceIdByName(name: String?, type: String): Int {
    var name = name
    if (name.isNullOrEmpty()) {
      return 0
    }
    name = name.lowercase(Locale.getDefault()).replace("-", "_")
    synchronized(ResourceUtils::class.java) {

      val context = ContextHolder.getInstance().getApplicationContext()
      val packageName = context.packageName
      return context.resources.getIdentifier(name, type, packageName)
    }
  }
}
