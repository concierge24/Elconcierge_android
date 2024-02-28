package com.trava.user.webservices.models

import android.animation.ValueAnimator
import com.google.android.gms.maps.model.Marker

class HomeMarkers {
    var marker: Marker? = null
    var moveValueAnimate: ValueAnimator? = null
    var rotateValueAnimator: ValueAnimator? = null
    var iconImageUrl : String? = ""
    var catrgory_brand_id:Int?= null
    var category_id:Int?= null
}