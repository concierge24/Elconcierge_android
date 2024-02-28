package com.trava.utilities

import android.util.Log

object Logger {

    val TAG = "Buraq24"

    private val DEBUGGING_BUILD = BuildConfig.DEBUG

    fun d(tag: String = TAG, message: Any) {
        if (DEBUGGING_BUILD)
            Log.d(tag, message.toString())
    }

    fun i(tag: String = TAG, message: String) {
        if (DEBUGGING_BUILD)
            Log.i(tag, message)
    }

    fun e(tag: String = TAG, message: Any) {
        if (DEBUGGING_BUILD) {
            Log.e(tag, "" + message.toString())

        }
    }

    fun e(tag: String = TAG, message: Any, exception: Exception) {
        if (DEBUGGING_BUILD) {
            Log.e(tag, message.toString(), exception.cause)

        }
    }

    fun e(tag: String = TAG, message: String, exception: Exception) {
        if (DEBUGGING_BUILD)
            Log.e(tag, message, exception)
    }
}