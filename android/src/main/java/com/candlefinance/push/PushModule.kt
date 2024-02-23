package com.candlefinance.push

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class PushModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  override fun initialize() {
    super.initialize()
    NotificationUtils.createDefaultChannelForFCM(reactApplicationContext)
    ContextHolder.getInstance().setApplicationContext(reactApplicationContext)
  }


  @ReactMethod
  fun getAuthorizationStatus(promise: Promise) {
    val context = reactApplicationContext.baseContext

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
      ) {
        promise.resolve(2) // authorized
      } else {
        promise.resolve(1) // denied
      }
    } else {
      promise.resolve(2) // authorized
    }
  }

  @ReactMethod
  fun requestPermissions(promise: Promise) {
   val context = reactApplicationContext.baseContext

   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED
        ) {
          registerForToken(promise)
        } else {
          val activity = reactApplicationContext.currentActivity

          if (activity is PermissionAwareActivity) {
            val currentRequestCode = 83834

            val listener = PermissionListener { requestCode: Int, _: Array<String>, grantResults: IntArray ->
              if (requestCode == currentRequestCode) {
                val isPermissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (isPermissionGranted) {
                  registerForToken(promise)
                  return@PermissionListener true
                }
                return@PermissionListener false
              }
              return@PermissionListener false
            }

            // Replace this with the appropriate permission for push notifications
            activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), currentRequestCode, listener)
          } else {
            promise.reject("NO_ACTIVITY", "No PermissionAwareActivity was found! Make sure the app has launched before calling this function.")
          }
        }

    } else {
       promise.resolve(true)
      }
  }

  @ReactMethod
  fun registerForToken(promise: Promise) {
    FirebaseMessaging.getInstance().isAutoInitEnabled=true;
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        RNEventEmitter.sendEvent(FirebaseMessagingService.errorReceived, "Fetching FCM registration token failed ${task.exception?.message}")
        return@OnCompleteListener
      }
      val token = task.result
      RNEventEmitter.sendEvent(FirebaseMessagingService.deviceTokenReceived, token)
    })
    promise.resolve(true)
  }

  @ReactMethod
  fun isRegisteredForRemoteNotifications(promise: Promise) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        RNEventEmitter.sendEvent(FirebaseMessagingService.errorReceived, "Fetching FCM registration token failed ${task.exception?.message}")
        promise.reject("NO_TOKEN", "No token found ${task.exception?.message}")
        return@OnCompleteListener
      }
      val token = task.result
      promise.resolve(true)
    })
  }

  @ReactMethod
  fun addListener(type: String?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  fun removeListeners(type: Int?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  companion object {
    const val NAME = "Push"
  }
}
