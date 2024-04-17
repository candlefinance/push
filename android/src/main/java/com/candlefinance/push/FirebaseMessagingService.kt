package com.candlefinance.push

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.google.firebase.messaging.FirebaseMessagingService

private val TAG = PushModule::class.java.simpleName
class PushNotificationHeadlessTaskService : HeadlessJsTaskService() {

  private val defaultTimeout: Long = 10000 // 10 seconds
 override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
   return NotificationPayload.fromIntent(intent)?.let {
     HeadlessJsTaskConfig(
       HEADLESS_TASK_KEY,
        Arguments.createMap().apply {
          putString("content", it.rawData.toString())
        },
       defaultTimeout, true
     )
   }
 }

  companion object {
    const val HEADLESS_TASK_KEY = "PushNotificationHeadlessTaskKey"
  }
}

class FirebaseMessagingService : FirebaseMessagingService() {

  private lateinit var utils: PushNotificationUtils

  override fun onCreate() {
    super.onCreate()
    utils = PushNotificationUtils(baseContext)
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    val params = Arguments.createMap()
    params.putString("token", token)
    Log.d(TAG, "Send device token event")
    PushNotificationEventManager.sendEvent(PushNotificationEventType.TOKEN_RECEIVED, params)
  }

  override fun handleIntent(intent: Intent) {
    val extras = intent.extras ?: Bundle()
    extras.getString("gcm.notification.body")?.let {
    // Use the notification body here
      // message contains push notification payload, show notification
//      onMessageReceived(it)
      Log.d(TAG, "**** Message: ${it}")
    } ?: run {
      Log.d(TAG, "Ignore intents that don't contain push notification payload")
      super.handleIntent(intent)
    }
  }

  private fun onMessageReceived(payload: NotificationPayload) {
    if (utils.isAppInForeground()) {
      Log.d(TAG, "Send foreground message received event")
      PushNotificationEventManager.sendEvent(
        PushNotificationEventType.FOREGROUND_MESSAGE_RECEIVED, Arguments.createMap().apply {
          putString("content", payload.rawData.toString())
        }
      )
    } else {
      Log.d(
        TAG, "App is in background, try to create notification and start headless service"
      )

      utils.showNotification(payload)

      try {
        val serviceIntent =
          Intent(baseContext, PushNotificationHeadlessTaskService::class.java)
        serviceIntent.putExtra("NotificationPayload", payload)
        if (baseContext.startService(serviceIntent) != null) {
          HeadlessJsTaskService.acquireWakeLockNow(baseContext)
        }
      } catch (exception: Exception) {
        Log.e(
          TAG, "Something went wrong while starting headless task: ${exception.message}"
        )
      }
    }
  }

}
