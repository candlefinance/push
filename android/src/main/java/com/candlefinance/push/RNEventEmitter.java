package com.candlefinance.push;

import android.util.Log;

import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNEventEmitter {

  private static final RNEventEmitter sharedInstance =
    new RNEventEmitter();

  public static RNEventEmitter getSharedInstance() {
    return sharedInstance;
  }


  static void sendEvent(String eventName, String eventMap) {
    var reactContext = ContextHolder.getApplicationContext();
    try {

      if (reactContext == null) {
        return;
      }

      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, eventMap);

    } catch (Exception e) {
      Log.e("SEND_EVENT", "", e);
    }
  }
}
