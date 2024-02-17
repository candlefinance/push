package com.candlefinance.push;

import android.content.Context;
import android.util.Log;

public class ContextHolder {
  private static Context applicationContext;

  public static Context getApplicationContext() {
    return applicationContext;
  }

  public static void setApplicationContext(Context applicationContext) {
    ContextHolder.applicationContext = applicationContext;
  }
}
