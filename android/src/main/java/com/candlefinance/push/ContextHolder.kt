package com.candlefinance.push
import com.facebook.react.bridge.ReactContext

class ContextHolder private constructor() {
  private lateinit var applicationContext: ReactContext

  companion object {

    @Volatile private var instance: ContextHolder? = null

    fun getInstance() =
      instance ?: synchronized(this) {
        instance ?: ContextHolder().also { instance = it }
      }
  }

  fun setApplicationContext(context: ReactContext) {
    if (!::applicationContext.isInitialized) {
      applicationContext = context
    }
  }

  fun getApplicationContext(): ReactContext? {
    if (!::applicationContext.isInitialized) {
      return null
    }
    return applicationContext
  }
}


