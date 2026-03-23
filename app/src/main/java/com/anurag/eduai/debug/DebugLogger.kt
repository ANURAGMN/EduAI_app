package com.anurag.eduai.debug

import android.util.Log


object DebugLogger {
    fun debugLog(tag: String, message: String){
//        if (BuildConfig.DEBUG){
            Log.d(tag, message)
//        }
    }

    fun errorLog(tag: String, message: String){
//        if (BuildConfig.DEBUG){
            Log.e(tag, message)
//        }
    }
}