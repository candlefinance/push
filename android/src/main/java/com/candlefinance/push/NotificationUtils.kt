package com.candlefinance.push

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.internal.ResourceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.net.URL

class PushNotificationsConstants {
  companion object {
    const val PINPOINT_PREFIX = "pinpoint" // pinpoint
    const val NOTIFICATION_PREFIX = "$PINPOINT_PREFIX.notification." // pinpoint.notification.
    const val CAMPAIGN_PREFIX = "$PINPOINT_PREFIX.campaign." // pinpoint.campaign.
    const val OPENAPP = "openApp" // openApp
    const val URL = "url" // url
    const val DEEPLINK = "deeplink" // deeplink
    const val TITLE = "title" // title
    const val MESSAGE = "message" // message
    const val IMAGEURL = "imageUrl" // imageUrl
    const val JOURNEY = "journey" // journey
    const val JOURNEY_ID = "journey_id" // journey_id
    const val JOURNEY_ACTIVITY_ID = "journey_activity_id" // journey_activity_id
    const val PINPOINT_OPENAPP = "$PINPOINT_PREFIX.$OPENAPP" // pinpoint.openApp
    const val PINPOINT_URL = "$PINPOINT_PREFIX.$URL" // pinpoint.url
    const val PINPOINT_DEEPLINK = "$PINPOINT_PREFIX.$DEEPLINK" // pinpoint.deeplink
    const val PINPOINT_NOTIFICATION_TITLE = "$NOTIFICATION_PREFIX$TITLE" // pinpoint.notification.title
    const val PINPOINT_NOTIFICATION_BODY = "${NOTIFICATION_PREFIX}body" // pinpoint.notification.body
    const val PINPOINT_NOTIFICATION_IMAGEURL = "$NOTIFICATION_PREFIX$IMAGEURL" // pinpoint.notification.imageUrl
    // pinpoint.notification.silentPush
    const val PINPOINT_NOTIFICATION_SILENTPUSH = "${NOTIFICATION_PREFIX}silentPush"
    const val CAMPAIGN_ID = "campaign_id" // campaign_id
    const val CAMPAIGN_ACTIVITY_ID = "campaign_activity_id" // campaign_activity_id
    const val PINPOINT_CAMPAIGN_CAMPAIGN_ID = "$CAMPAIGN_PREFIX$CAMPAIGN_ID" // pinpoint.campaign.campaign_id
    // pinpoint.campaign.campaign_activity_id
    const val PINPOINT_CAMPAIGN_CAMPAIGN_ACTIVITY_ID = "$CAMPAIGN_PREFIX$CAMPAIGN_ACTIVITY_ID"
    const val DEFAULT_NOTIFICATION_CHANNEL_ID = "PINPOINT.NOTIFICATION" // PINPOINT.NOTIFICATION
    const val DIRECT_CAMPAIGN_SEND = "_DIRECT" // _DIRECT
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

  fun isAppInForeground(): Boolean {
    // Gets a list of running processes.
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val processes = am.runningAppProcesses

    // On some versions of android the first item in the list is what runs in the foreground, but this is not true
    // on all versions. Check the process importance to see if the app is in the foreground.
    val packageName = context.applicationContext.packageName
    for (appProcess in processes) {
      val processName = appProcess.processName
      if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND == appProcess.importance && packageName == processName) {
        return true
      }
    }
    return false
  }

  fun areNotificationsEnabled(): Boolean {
    // check for app level opt out
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
  }

  @Suppress("DEPRECATION")
  @SuppressLint("NewApi")
  fun showNotification(
    notificationId: Int,
    payload: NotificationPayload,
    targetClass: Class<*>?
  ) {
    CoroutineScope(Dispatchers.IO).launch {
//      val largeImageIcon = payload.imageUrl?.let { downloadImage(it) }
      val notificationIntent = Intent(context, payload.targetClass ?: targetClass)
      notificationIntent.putExtra("amplifyNotificationPayload", payload)
      notificationIntent.putExtra("notificationId", notificationId)
      val pendingIntent = PendingIntent.getActivity(
        context,
        notificationId,
        notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      val notificationChannel = retrieveNotificationChannel()
      val builder = if (isNotificationChannelSupported() && notificationChannel != null) {
        NotificationCompat.Builder(context, payload.channelId ?: notificationChannel.id)
      } else {
        NotificationCompat.Builder(context)
      }

      builder.apply {
//        setContentTitle(payload.title)
//        setContentText(payload.body)
        setSmallIcon(R.drawable.ic_default_notification)
        setContentIntent(pendingIntent)
        setPriority(NotificationCompat.PRIORITY_DEFAULT)
//        setLargeIcon(largeImageIcon)
        setAutoCancel(true)
      }

      with(NotificationManagerCompat.from(context)) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//          // TODO: Consider calling
//          //    ActivityCompat#requestPermissions
//          // here to request the missing permissions, and then overriding
//          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//          //                                          int[] grantResults)
//          // to handle the case where the user grants the permission. See the documentation
//          // for ActivityCompat#requestPermissions for more details.
//        } else {
//          notify(notificationId, builder.build())
//        }
      }
    }
  }
}

class PushNotificationUtils(context: Context) {
    private val utils = PushNotificationsUtils(context)

    fun showNotification(
        payload: NotificationPayload
    ) {
        // TODO:
    }

    fun isAppInForeground(): Boolean {
        return utils.isAppInForeground()
    }
}

@Parcelize
open class NotificationContentProvider internal constructor(open val content: Map<String, String>) : Parcelable {
  @Parcelize
  class FCM(override val content: Map<String, String>) : NotificationContentProvider(content)
}

@Parcelize
open class NotificationPayload(
  val contentProvider: NotificationContentProvider,
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

  companion object {
    @JvmStatic
    fun builder(contentProvider: NotificationContentProvider) = Builder(contentProvider)

    inline operator fun invoke(
      contentProvider: NotificationContentProvider,
      block: Builder.() -> Unit
    ) = Builder(contentProvider).apply(block).build()

    @JvmStatic
    fun fromIntent(intent: Intent?): NotificationPayload? {
      return intent?.getParcelableExtra("amplifyNotificationPayload")
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
  object Granted : PermissionRequestResult
  data class NotGranted(val shouldShowRationale: Boolean) : PermissionRequestResult
}
