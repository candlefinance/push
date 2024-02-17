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

  private  val CHANNEL_ID = "RNFireDefaultChannelID"
  private  val CHANNEL_NAME = "Firebase Default"
  private val NOTIFICATION_ID = 123321

  fun createDefaultNotificationChannel (context:Context){
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
      )
      channel.description = "All by default notification channel for all firebase notification"
      notificationManager.createNotificationChannel(channel)
    }

  }
  fun sendNotification(context: Context, title: String, message: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(getResourceIdByName("ic_launcher","mipmap"))
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT).setChannelId(CHANNEL_ID)

    notificationManager.notify(NOTIFICATION_ID, builder.build())
  }

  @SuppressLint("DiscouragedApi")
  fun getResourceIdByName(name: String?, type: String): Int {
    var name = name
    if (name.isNullOrEmpty()) {
      return 0
    }
    name = name.lowercase(Locale.getDefault()).replace("-", "_")
    val key = name + "_" + type
    synchronized(ResourceUtils::class.java) {

      val context = ContextHolder.getApplicationContext()
      val packageName = context.packageName
      return context.resources.getIdentifier(name, type, packageName)
    }
  }
}
