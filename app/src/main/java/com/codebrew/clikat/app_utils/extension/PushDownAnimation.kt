package com.codebrew.clikat.app_utils.extension

import android.os.SystemClock
import android.view.View
import com.thekhaeng.pushdownanim.PushDownAnim

fun View.pushDownClickListener(onSafeClick: View.OnClickListener) {
    PushDownAnim.setPushDownAnimTo(this)
            .setScale(PushDownAnim.MODE_SCALE,
                    PushDownAnim.DEFAULT_PUSH_SCALE)
            .setDurationPush(PushDownAnim.DEFAULT_PUSH_DURATION)
            .setDurationRelease(PushDownAnim.DEFAULT_RELEASE_DURATION)
            .setInterpolatorPush(PushDownAnim.DEFAULT_INTERPOLATOR)
            .setInterpolatorRelease(PushDownAnim.DEFAULT_INTERPOLATOR)
            .setOnClickListener {
                onSafeClick.onClick(this)
            }
}