package com.candlefinance.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.net.URL

class PushNotificationsConstants {
  companion object {
    const val OPENAPP = "openApp" // openApp
    const val URL = "url" // url
    const val DEEPLINK = "deeplink" // deeplink
    const val TITLE = "title" // title
    const val BODY = "body" // body
    const val SUBTITLE = "subtitle" // subtitle
    const val PRIORITY = "priority" // priority
    const val IMAGEURL = "imageUrl" // imageUrl
    const val DEFAULT_NOTIFICATION_CHANNEL_ID = "default_notification_channel_id" // default_notification_channel_id
  }
}

class PushNotificationsUtils(
  private val context: Context,
  private val channelId: String = PushNotificationsConstants.DEFAULT_NOTIFICATION_CHANNEL_ID
) {
  init {
    retrieveNotificationChannel()
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
  private fun isNotificationChannelSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

  private fun retrieveNotificationChannel(): NotificationChannel? {
    var channel: NotificationChannel? = null
    val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
    if (isNotificationChannelSupported()) {
      channel = notificationManager?.getNotificationChannel(channelId)
    }
    return channel ?: createDefaultNotificationChannel(channelId)
  }

  // create before notification trigger for API 32 or lower
  @SuppressLint("NewApi")
  private fun createDefaultNotificationChannel(channelId: String): NotificationChannel? {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (isNotificationChannelSupported()) {
      val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
      val defaultChannel = NotificationChannel(
        channelId,
        "Default channel",
        NotificationManager.IMPORTANCE_DEFAULT
      )
      // Register the channel with the system
      notificationManager?.createNotificationChannel(defaultChannel)
      return defaultChannel
    }
    return null
  }

  private suspend fun downloadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
    BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
  }

  @SuppressLint("DiscouragedApi")
  fun getResourceIdByName(name: String, type: String): Int {
    return context.resources.getIdentifier(name, type, context.packageName)
  }

  @SuppressLint("NewApi")
  fun showNotification(
    notificationId: Int,
    payload: NotificationPayload,
    targetClass: Class<*>?
  ) {
    Log.d(Tag, "Show notification with payload: ${payload.rawData}")
    CoroutineScope(Dispatchers.IO).launch {
      val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
      val notificationBuilder = NotificationCompat.Builder(context, channelId)
      val notificationContent = payload.rawData

      notificationBuilder
              .setSmallIcon(getResourceIdByName("ic_default_notification_foreground", "drawable"))
              .setContentTitle(notificationContent[PushNotificationsConstants.TITLE])
              .setContentText(notificationContent[PushNotificationsConstants.BODY])
              .setSubText(notificationContent[PushNotificationsConstants.SUBTITLE])
              .setPriority(notificationContent[PushNotificationsConstants.PRIORITY]?.toInt() ?: NotificationCompat.PRIORITY_DEFAULT)
              .setAutoCancel(true)

      Log.d(Tag, "targetClass: $targetClass")
      if (targetClass != null) {
        Log.d(Tag, "targetClass is not null")
        val intent = Intent(context, targetClass).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          val url = notificationContent[PushNotificationsConstants.URL]
          val deepLink = notificationContent[PushNotificationsConstants.DEEPLINK]

          if (url != null) {
            putExtra(PushNotificationsConstants.URL, url)
          }

          if (deepLink != null) {
            putExtra(PushNotificationsConstants.DEEPLINK, deepLink)
          }
          putExtra(PushNotificationsConstants.OPENAPP, true)
          val json = JSONObject()
          notificationContent.forEach { (key, value) -> json.put(key, value) }
          Log.d(Tag, "SAVE to intent rawData: $json")
          putExtra("rawData", json.toString())
        }

        notificationBuilder.setContentIntent(
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intent,
                  PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
                )
        )
      } else {
        Log.e(Tag, "targetClass is null")
      }

      if (notificationContent.containsKey(PushNotificationsConstants.IMAGEURL)) {
        val imageUrl = notificationContent[PushNotificationsConstants.IMAGEURL]
        val bitmap = imageUrl?.let { downloadImage(it) }
        if (bitmap != null) {
          notificationBuilder.setLargeIcon(bitmap)
        }
      }

      if (isNotificationChannelSupported()) {
        notificationBuilder.setChannelId(channelId)
      }

      notificationManager?.notify(notificationId, notificationBuilder.build())
    }
  }
}

private const val Tag = "PushNotificationUtils"

class PushNotificationUtils(context: Context) {
    private val utils = PushNotificationsUtils(context)
    private val lifecycleObserver = AppLifecycleListener()

    init {
        if (context is LifecycleOwner) {
            Log.d(Tag, "Add lifecycle observer to context")
            context.lifecycle.addObserver(lifecycleObserver)
        } else {
            Log.e(Tag, "Context is not a lifecycle owner")
          ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        }
    }

    fun showNotification(
        payload: NotificationPayload
    ) {
        Log.d(Tag, "Show notification with payload: $payload")

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val targetClass = payload.rawData["targetClass"]?.let {
          Log.d(Tag, "targetClass: $it")
          try {
            Class.forName(it)
          } catch (e: ClassNotFoundException) {
            Log.e(Tag, "Class not found: $it")
            null
          }
        }
        if (targetClass != null) {
          utils.showNotification(notificationId, payload, targetClass)
        } else {
          Log.e(Tag, "targetClass is null")
        }
    }

    fun isAppInForeground(): Boolean {
        return lifecycleObserver.isAppInForeground
    }

    fun isAppInBackground(): Boolean {
        return lifecycleObserver.isAppInBackground
    }
}

@Parcelize
open class NotificationContentProvider internal constructor(open val content: Map<String, String>) : Parcelable {
  @Parcelize
  class FCM(override val content: Map<String, String>) : NotificationContentProvider(content)
}

@Parcelize
open class NotificationPayload(
        private val contentProvider: NotificationContentProvider,
        val channelId: String? = null,
        val targetClass: Class<*>? = null
) : Parcelable {

  @IgnoredOnParcel
  val rawData: Map<String, String> = extractRawData()

  internal constructor(builder: Builder) : this(builder.contentProvider, builder.channelId, builder.targetClass)

  private fun extractRawData() = when (contentProvider) {
    is NotificationContentProvider.FCM -> contentProvider.content
    else -> mapOf()
  }

  fun toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    if (rawData.containsKey("rawData")) {
      val existingRawData = rawData["rawData"]?.let {
        JSONObject(it)
      }
      val toMap = existingRawData?.let {
        it.keys().asSequence().associateWith { key -> it.get(key).toString() }
      }
      toMap?.forEach { (key, value) -> map.putString(key, value) }
      map.putMap("rawData", Arguments.makeNativeMap(toMap))
      Log.d(Tag, "TO READABLE existing: $map")
    } else {
      map.putMap("rawData", Arguments.makeNativeMap(rawData))
      rawData.forEach { (key, value) -> map.putString(key, value) }
      Log.d(Tag, "TO READABLE new: $map")
    }
    map.putString("channelId", channelId)
    return map
  }

  companion object {
    @JvmStatic
    fun builder(contentProvider: NotificationContentProvider) = Builder(contentProvider)

    inline operator fun invoke(
      contentProvider: NotificationContentProvider,
      block: Builder.() -> Unit
    ) = Builder(contentProvider).apply(block).build()

    @JvmStatic
    fun fromIntent(intent: Intent?): NotificationPayload? {
      return intent?.extras?.let {
        val toMap: Map<String, String?> = it.keySet().associateWith { key -> it.get(key)?.toString() }
        val contentProvider = NotificationContentProvider.FCM(toMap.filterValues { it != null } as Map<String, String>)
        Log.d(Tag, "READING: Notification payload from intent: $toMap")
        NotificationPayload(contentProvider)
      }
    }
  }

  class Builder(val contentProvider: NotificationContentProvider) {
    var channelId: String? = null
      private set
    var targetClass: Class<*>? = null
      private set

    fun notificationChannelId(channelId: String?) = apply { this.channelId = channelId }

    fun targetClass(targetClass: Class<*>?) = apply { this.targetClass = targetClass }

    fun build() = NotificationPayload(this)
  }
}

sealed interface PermissionRequestResult {
  data object Granted : PermissionRequestResult
  data class NotGranted(val shouldShowRationale: Boolean) : PermissionRequestResult
}

class AppLifecycleListener : DefaultLifecycleObserver {
  var isAppInForeground: Boolean = false
  var isAppInBackground: Boolean = false

  override fun onStart(owner: LifecycleOwner) {
    // App moved to foreground
    println("App is in the foreground")

    isAppInForeground = true
    isAppInBackground = false
  }

  override fun onStop(owner: LifecycleOwner) {
    // App moved to background
    println("App is in the background")
    isAppInBackground = true
    isAppInForeground = false
  }
}
