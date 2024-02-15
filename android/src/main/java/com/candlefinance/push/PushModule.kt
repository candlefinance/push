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

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }



  @ReactMethod
  fun requestPermissions(promise: Promise) {
   val context = reactApplicationContext.baseContext

   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED
        ) {
          promise
            .resolve(true)
        } else {
          val activity = reactApplicationContext.currentActivity

          if (activity is PermissionAwareActivity) {
            val currentRequestCode = 83834

            val listener = PermissionListener { requestCode: Int, _: Array<String>, grantResults: IntArray ->
              if (requestCode == currentRequestCode) {
                val permissionStatus = if (grantResults.isNotEmpty()) grantResults[0] else PackageManager.PERMISSION_DENIED
                promise.resolve(permissionStatus== PackageManager.PERMISSION_GRANTED)
                return@PermissionListener true
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
  fun getToken(promise: Promise) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        Log.w("getTokenError", "Fetching FCM registration token failed", task.exception)
        promise.reject(task.exception)
        return@OnCompleteListener
      }

      // Get new FCM registration token
      val token = task.result
      promise.resolve(token)
    })
  }

  companion object {
    const val NAME = "Push"
  }
}
