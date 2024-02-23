package com.candlefinance.push

import android.util.Log
import com.facebook.react.modules.core.DeviceEventManagerModule

object RNEventEmitter {
    fun sendEvent(eventName: String?, eventMap: String?) {
        val reactContext = ContextHolder.getInstance().getApplicationContext()
        try {
          reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)?.emit(eventName!!, eventMap)
        } catch (e: Exception) {
            Log.e("SEND_EVENT", "", e)
        }
    }
}
