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
    })

    formattedData.put("kind", getAppState())
    formattedData.put("uuid", UUID.randomUUID().toString())
    formattedData.put("custom", remoteMessage.data.toString())

    // Send the event
    RNEventEmitter.sendEvent(notificationReceived, formattedData.toString())
    NotificationUtils
      .sendNotification(
        ContextHolder.getInstance().getApplicationContext(), title, body
      )
    if (remoteMessage.data.isNotEmpty()) {
      Log.d(TAG, "Message data payload: ${remoteMessage.data}")
    }
    remoteMessage.notification?.let {
      Log.d(TAG, "Message Notification Body: ${it.body}")
    }
  }

  // TODO: make this actually work
  private fun getAppState(): String {
    val reactContext = ContextHolder.getInstance().getApplicationContext()
    val currentActivity = reactContext.currentActivity
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
