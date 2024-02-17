package com.candlefinance.push;
import com.facebook.react.bridge.ReactContext;

public class ContextHolder {
  private static ReactContext applicationContext;

  public static ReactContext getApplicationContext() {
    return applicationContext;
  }

  public static void setApplicationContext(ReactContext applicationContext) {
    ContextHolder.applicationContext = (ReactContext) applicationContext;
  }
}
