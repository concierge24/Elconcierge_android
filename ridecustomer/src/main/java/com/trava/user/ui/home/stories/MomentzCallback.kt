package com.trava.user.ui.home.stories

import android.view.View

interface MomentzCallback{
    fun done()

    fun onNextCalled(view: View, momentz : Momentz, index: Int)

    fun onSkip()
    fun onNextView()

    fun onSwipeUp()

    fun onSwipeDown()

}
