package com.candlefinance.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.lang.Exception
import java.util.UUID

class FirebaseMessagingService : FirebaseMessagingService() {

  override fun onSendError(msgId: String, exception: Exception) {
    super.onSendError(msgId, exception)
    RNEventEmitter.sendEvent(errorReceived, exception.message)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title = remoteMessage.notification?.title.toString()
    val body = remoteMessage.notification?.body.toString()

    val formattedData = JSONObject()

    val apsData = JSONObject()
    apsData.put("alert", JSONObject().apply {
        put("title", title)
        put("body", body)
    })
    formattedData.put("payload", JSONObject().apply {
        put("aps", apsData)
        put("custom", JSONObject(mapToString(remoteMessage.data)))
    })

    formattedData.put("kind", getAppState())
    formattedData.put("uuid", UUID.randomUUID().toString())

    // Send the event
    RNEventEmitter.sendEvent(notificationReceived, formattedData.toString())
    ContextHolder.getInstance().getApplicationContext()?.let {
      NotificationUtils
        .sendNotification(
          it, title, body
        )
    }
    if (remoteMessage.data.isNotEmpty()) {
      Log.d(TAG, "Message data payload: ${remoteMessage.data}")
    }
    remoteMessage.notification?.let {
      Log.d(TAG, "Message Notification Body: ${it.body}")
    }
  }

  private fun mapToString(map: Map<String, String>): String {
    val json = JSONObject()
    for ((key, value) in map) {
      json.put(key, value)
    }
    return json.toString()
  }

  // TODO: make this actually work
  private fun getAppState(): String {
    val reactContext = ContextHolder.getInstance().getApplicationContext()
    val currentActivity = reactContext?.currentActivity
    return if (currentActivity == null) {
      "background"
    } else {
      "foreground"
    }
  }

  override fun onNewToken(token: String) {
    Log.d(TAG,token)
    RNEventEmitter.sendEvent(deviceTokenReceived, token)
  }

  companion object {
    private const val TAG = "MyFirebaseMsgService"
    const val notificationReceived = "notificationReceived"
    const val deviceTokenReceived = "deviceTokenReceived"
    const val errorReceived = "errorReceived"
  }
}
