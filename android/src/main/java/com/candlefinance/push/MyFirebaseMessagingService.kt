package com.candlefinance.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

  // fun onSendError(messageId: String?, sendError: Exception?) {
  // //TODO handler error
  //     Log.d(TAG,"on error")
  // }
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title = remoteMessage.notification?.title.toString();
    val body = remoteMessage.notification?.body.toString();

    // TODO remove after testing 
    RNEventEmitter.sendEvent("deviceTokenReceived",title)

    NotificationUtils.sendNotification(ContextHolder.getApplicationContext(),title,body)
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
      RNEventEmitter.sendEvent("deviceTokenReceived",token)
    }
  companion object {
    private const val TAG = "MyFirebaseMsgService"
  }


}
