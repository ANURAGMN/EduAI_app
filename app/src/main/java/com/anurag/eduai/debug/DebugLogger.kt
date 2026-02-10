package com.anurag.eduai.debug

import android.util.Log
import com.anurag.eduai.BuildConfig


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