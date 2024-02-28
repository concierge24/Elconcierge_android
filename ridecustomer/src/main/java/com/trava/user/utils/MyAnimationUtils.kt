package com.trava.user.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.transition.Explode
import androidx.transition.Transition
import com.trava.utilities.Utils
import com.trava.utilities.hide
import com.trava.utilities.show
import kotlinx.android.synthetic.main.fragment_services.*

class MyAnimationUtils {

    companion object{

        fun animateShowHideView(animContainer : ViewGroup, actionView : View,action : Boolean ){
            TransitionManager.beginDelayedTransition(animContainer)
            if(action){
                /* show view */
                actionView.show()

            }else{
                /* hide view */
                actionView.hide()
            }
        }

        fun explodeViewAnimation(activity : Activity,view : View, angle : Int) : Explode{
            val explode = Explode()
            explode.excludeChildren(view, true)
            val viewRect = Rect()
            viewRect.set(0, 0, activity.let { it1 -> Utils.getScreenWidth(it1) }, Utils.dpToPx(angle).toInt())
            explode.epicenterCallback = object : Transition.EpicenterCallback() {
                override fun onGetEpicenter(transition: Transition): Rect {
                    return viewRect
                }
            }
            return explode
        }
    }
}