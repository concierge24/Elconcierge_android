package com.codebrew.clikat.app_utils.extension

import android.os.SystemClock
import android.view.View

fun View.setSafeOnClickListener(onSafeClick: View.OnClickListener) {

    var lastTimeClicked: Long = 0

    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < 500) {
            return@setOnClickListener
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeClick.onClick(this)
    }
}