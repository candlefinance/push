package com.candlefinance.push

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.google.firebase.messaging.FirebaseMessagingService
import org.json.JSONObject

private val TAG = PushModule::class.java.simpleName
class PushNotificationHeadlessTaskService : HeadlessJsTaskService() {

  private val defaultTimeout: Long = 10000 // 10 seconds
 override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
   return NotificationPayload.fromIntent(intent)?.let {
     Log.d(TAG, "Starting headless task with payload: $it")
     HeadlessJsTaskConfig(
       HEADLESS_TASK_KEY,
        it.toWritableMap(),
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
    val payload = NotificationPayload.fromIntent(intent)
    if (payload != null) {
      onMessageReceived(payload)
      Log.d(TAG, "Notification payload found in intent $payload")
    } else {
      Log.d(TAG, "No notification payload found in intent")
    }
  }

  private fun onMessageReceived(payload: NotificationPayload) {
    Log.d(TAG, "Received new message: $payload")
    if (utils.isAppInForeground()) {
      Log.d(TAG, "Send foreground message received event with payload: $payload")
      PushNotificationEventManager.sendEvent(
              PushNotificationEventType.FOREGROUND_MESSAGE_RECEIVED, payload.toWritableMap()
      )
    } else if (utils.isAppInBackground()) {
      Log.d(TAG, "App is in background but in memory, send background message received event with payload: $payload")
      PushNotificationEventManager.sendEvent(
              PushNotificationEventType.BACKGROUND_MESSAGE_RECEIVED, payload.toWritableMap()
      )
      utils.showNotification(payload)
    } else {
      Log.d(TAG, "App is killed, start HeadlessJsTaskService with payload: $payload")
      utils.showNotification(payload)

      try {
        val serviceIntent = Intent(baseContext, PushNotificationHeadlessTaskService::class.java)
        val json = JSONObject()
        json.put("rawData", payload.rawData)
        payload.rawData.forEach { (key, value) ->
          json.put(key, value)
        }
        serviceIntent.putExtra("data", json.toString())
        if (baseContext.startService(serviceIntent) != null) {
          HeadlessJsTaskService.acquireWakeLockNow(baseContext)
        } else {
          Log.e(TAG, "Failed to start headless task")
        }
      } catch (exception: Exception) {
        Log.e(TAG, "Something went wrong while starting headless task: ${exception.message}")
      }
    }
  }

}
