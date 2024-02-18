package com.candlefinance.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception

class FirebaseMessagingService : FirebaseMessagingService() {
  override fun onSendError(msgId: String, exception: Exception) {
    super.onSendError(msgId, exception)
    RNEventEmitter.sendEvent(errorReceived,exception.message)
  }
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title = remoteMessage.notification?.title.toString()
    val body = remoteMessage.notification?.body.toString()
    NotificationUtils.sendNotification(ContextHolder.getInstance().getApplicationContext(),title,body)
    if (remoteMessage.data.isNotEmpty()) {
      Log.d(TAG, "Message data payload: ${remoteMessage.data}")
    }
    // Check if message contains a notification payload.
    remoteMessage.notification?.let {
      Log.d(TAG, "Message Notification Body: ${it.body}")
    }
  }

    override fun onNewToken(token: String) {
      Log.d(TAG,token)
      RNEventEmitter.sendEvent(deviceTokenReceived,token)
    }
  companion object {
    private const val TAG = "MyFirebaseMsgService"
//    const val notificationReceived = "notificationReceived"
    const val deviceTokenReceived = "deviceTokenReceived"
    const val errorReceived = "errorReceived"
  }
}
