package com.candlefinance.push

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

private val TAG = PushModule::class.java.simpleName
private const val PERMISSION = "android.permission.POST_NOTIFICATIONS"
private const val PREF_FILE_KEY = "com.candlefinance.push.pushnotification"
private const val PREF_PREVIOUSLY_DENIED = "wasPermissionPreviouslyDenied"

enum class PushNotificationPermissionStatus {
  NotDetermined,
  Authorized,
  Denied,
}

class PushModule(
  reactContext: ReactApplicationContext,
  dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ReactContextBaseJavaModule(reactContext), ActivityEventListener, LifecycleEventListener {

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Push"
  }

  private var launchNotification: WritableMap? = null
  private val sharedPreferences = reactContext.getSharedPreferences(PREF_FILE_KEY, MODE_PRIVATE)
  private val scope = CoroutineScope(dispatcher)

  init {
    reactContext.addActivityEventListener(this)
    reactContext.addLifecycleEventListener(this)
  }

  @ReactMethod
  fun registerForToken(promise: Promise) {
    FirebaseMessaging.getInstance().isAutoInitEnabled=true;
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        Log.d(TAG,"Fetching FCM registration token failed ${task.exception?.message}")
        return@OnCompleteListener
      }
      val token = task.result
      PushNotificationEventManager.sendEvent(PushNotificationEventType.TOKEN_RECEIVED, Arguments.createMap().apply {
        putString("token", token)
      })
    })
    promise.resolve(true)
  }

  @ReactMethod
  fun getLaunchNotification(promise: Promise) {
    launchNotification?.let {
      promise.resolve(launchNotification)
      launchNotification = null
    } ?: promise.resolve(null)
  }

  @ReactMethod
  fun getPermissionStatus(promise: Promise) {
    val permission = reactApplicationContext.currentActivity?.let { PushNotificationPermission(it) }
    // If permission has already been granted
    if (permission != null) {
      if (permission.hasRequiredPermission) {
        return promise.resolve(PushNotificationPermissionStatus.Authorized.name)
      }
    }
    // If the shouldShowRequestPermissionRationale flag is true, permission must have been
    // denied once (and only once) previously
    if (shouldShowRequestPermissionRationale()) {
      return promise.resolve(PushNotificationPermissionStatus.NotDetermined.name)
    }
    // If the shouldShowRequestPermissionRationale flag is false and the permission was
    // already previously denied then user has denied permissions twice
    if (sharedPreferences.getBoolean(PREF_PREVIOUSLY_DENIED, false)) {
      return promise.resolve(PushNotificationPermissionStatus.Denied.name)
    }
    // Otherwise it's never been requested (or user could have dismissed the request without
    // explicitly denying)
    promise.resolve(PushNotificationPermissionStatus.NotDetermined.name)
  }

  @ReactMethod
  fun requestPermissions(
    @Suppress("UNUSED_PARAMETER") permissions: ReadableMap,
    promise: Promise
  ) {
    scope.launch {
      val permission = reactApplicationContext.currentActivity?.let { PushNotificationPermission(it) }
      val result = permission?.requestPermission()
      if (result is PermissionRequestResult.Granted) {
        promise.resolve(true)
      } else {
        // If permission was not granted and the shouldShowRequestPermissionRationale flag
        // is true then user must have denied for the first time. We will set the
        // wasPermissionPreviouslyDenied value to true only in this scenario since it's
        // possible to dismiss the permission request without explicitly denying as well.
        if (shouldShowRequestPermissionRationale()) {
          with(sharedPreferences.edit()) {
            putBoolean(PREF_PREVIOUSLY_DENIED, true)
            apply()
          }
        }
        promise.resolve(false)
      }
    }
  }

  @ReactMethod
  fun addListener(@Suppress("UNUSED_PARAMETER") eventName: String) {
    // noop - only required for RN NativeEventEmitter
  }

  @ReactMethod
  fun removeListeners(@Suppress("UNUSED_PARAMETER") count: Int) {
    // noop - only required for RN NativeEventEmitter
  }

  override fun getConstants(): MutableMap<String, Any> = hashMapOf(
    "NativeEvent" to PushNotificationEventType.values().associateBy({ it.name }, { it.value }),
    "NativeHeadlessTaskKey" to PushNotificationHeadlessTaskService.HEADLESS_TASK_KEY
  )

  override fun onActivityResult(p0: Activity?, p1: Int, p2: Int, p3: Intent?) {
    // noop - only overridden as this class implements ActivityEventListener
  }

  /**
   * Send notification opened app event to JS layer if the app is in a background state
   */
  override fun onNewIntent(intent: Intent) {
    Log.d(TAG, "New intent received")
    val payload = NotificationPayload.fromIntent(intent)
    if (payload != null) {
      PushNotificationEventManager.sendEvent(
        PushNotificationEventType.NOTIFICATION_OPENED, payload.toWritableMap()
      )
    } else {
      Log.d(TAG, "No notification payload found in intent")
    }
  }

  /**
   * On every app resume (including launch), send the current device token to JS layer. Also
   * store the app launching notification if app is in a quit state
   */
  override fun onHostResume() {
    Log.d(TAG, "App resumed")
    PushNotificationEventManager.init(reactApplicationContext)
    currentActivity?.intent?.let {
        val payload = NotificationPayload.fromIntent(it)
        if (payload != null) {
          Log.d(TAG, "Launch notification found in intent waiting 5 seconds")
          launchNotification = payload.toWritableMap()
          Handler(Looper.getMainLooper()).postDelayed({
            PushNotificationEventManager.sendEvent(
                    PushNotificationEventType.LAUNCH_NOTIFICATION_OPENED,
                    payload.toWritableMap()
            )
          }, 3000)
        } else {
          Log.d(TAG, "No launch notification found in intent")
        }
      }
  }

  override fun onHostPause() {
    // noop - only overridden as this class implements LifecycleEventListener
    Log.d(TAG, "App paused")
  }

  override fun onHostDestroy() {
    scope.cancel()
  }

  private fun shouldShowRequestPermissionRationale(): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(currentActivity!!, PERMISSION)
  }
}

internal const val PermissionRequiredApiLevel = 33
internal const val PermissionName = "android.permission.POST_NOTIFICATIONS"

class PushNotificationPermission(private val context: Context) {

  val hasRequiredPermission: Boolean
    get() = Build.VERSION.SDK_INT < PermissionRequiredApiLevel ||
      ContextCompat.checkSelfPermission(context, PermissionName) == PackageManager.PERMISSION_GRANTED

  /**
   * Launches an Activity to request notification permissions and suspends until the user makes a selection or
   * dismisses the dialog. The behavior of this function depends on the device, current permission status, and
   * build configuration.
   *
   * 1. If the device API level is < 33 then this will immediately return [PermissionRequestResult.Granted] because
   *    no permission is required on this device.
   * 2. If the device API level is >= 33 but the application is targeting API level < 33 then this function will not
   *    show a permission dialog, but will return the current status of the notification permission. The permission
   *    request dialog will instead appear whenever the app tries to create a notification channel.
   * 3. Otherwise, the dialog will be shown or not as per normal runtime permission request rules
   * See https://developer.android.com/develop/ui/views/notifications/notification-permission for details
   */
  suspend fun requestPermission(): PermissionRequestResult {
    if (hasRequiredPermission) {
      return PermissionRequestResult.Granted
    }

    val requestId = UUID.randomUUID().toString()
    Log.d(TAG, "Requesting notification permission with requestId: $requestId")
    // Check if the context is an instance of Activity
    if (context is Activity) {
      Log.d(TAG, "Requesting notification permission")
      // Check if the version is Android 12 or higher
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Log.d(TAG, "Requesting notification permission on Android 12 or higher")
        // Request the permission
        Log.d(TAG, "Requesting notification permission on Android 12 or higher")
        ActivityCompat.requestPermissions(
          context,
          arrayOf(Manifest.permission.POST_NOTIFICATIONS),
          Math.abs(requestId.hashCode())
        )
        Log.d(TAG, "Requesting notification permission on Android 12 or higher")
      }

      // Listen for the result
      return PermissionRequestChannel.listen(requestId).first()
    } else {
      Log.e(TAG, "Context is not an instance of Activity")
      throw IllegalStateException("Context is not an instance of Activity")
    }
  }
}

internal object PermissionRequestChannel {
  private class IdAndResult(val requestId: String, val result: PermissionRequestResult)

  private val flow = MutableSharedFlow<IdAndResult>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  /**
   * Get a flow for the result of a particular permission request
   */
  fun listen(requestId: String) = flow.filter { it.requestId == requestId }.map { it.result }

  /**
   * Send the result of a permission request
   */
  fun send(requestId: String, result: PermissionRequestResult) = flow.tryEmit(IdAndResult(requestId, result))
}
