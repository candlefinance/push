package com.candlefinance.push

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

enum class PushNotificationEventType(val value: String) {
  FOREGROUND_MESSAGE_RECEIVED("ForegroundMessageReceived"),
  LAUNCH_NOTIFICATION_OPENED("LaunchNotificationOpened"),
  NOTIFICATION_OPENED("NotificationOpened"),
  BACKGROUND_MESSAGE_RECEIVED("BackgroundMessageReceived"),
  TOKEN_RECEIVED("TokenReceived"),
  FAILED_TO_REGISTER("FailedToRegister")
}

class PushNotificationEvent(val type: PushNotificationEventType, val params: WritableMap?)

object PushNotificationEventManager {
  private lateinit var reactContext: ReactApplicationContext
  private var isInitialized: Boolean = false
  private val eventQueue: MutableList<PushNotificationEvent> = mutableListOf()

  fun init(reactContext: ReactApplicationContext) {
    this.reactContext = reactContext
    isInitialized = true
    flushEventQueue()
  }

  fun sendEvent(type: PushNotificationEventType, params: WritableMap?) {
    if (!isInitialized) {
      eventQueue.add(PushNotificationEvent(type, params))
    } else {
      Log.d("PushNotificationEventManager", "Sending event: $type")
      sendJSEvent(type, params)
    }
  }

  private fun sendJSEvent(type: PushNotificationEventType, params: WritableMap?) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      ?.emit(type.value, params)
  }

  private fun flushEventQueue() {
    eventQueue.forEach {
      sendJSEvent(it.type, it.params)
    }
    eventQueue.clear()
  }
}
