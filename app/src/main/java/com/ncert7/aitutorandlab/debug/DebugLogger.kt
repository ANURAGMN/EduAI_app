package com.ncert7.aitutorandlab.debug

import android.util.Log
import com.ncert7.aitutorandlab.BuildConfig


object DebugLogger {
    fun debugLog(tag: String, message: String){
       if (BuildConfig.DEBUG){
            Log.d(tag, message)
       }
    }

    fun errorLog(tag: String, message: String){
        if (BuildConfig.DEBUG){
            Log.e(tag, message)
       }
    }

    fun warnLog(tag: String, message: String){
      if (BuildConfig.DEBUG){
            Log.w(tag, message)
      }
    }
}