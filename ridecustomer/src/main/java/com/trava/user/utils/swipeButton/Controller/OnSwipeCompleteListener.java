package com.trava.user.utils.swipeButton.Controller;


import com.trava.user.utils.swipeButton.View.LeftSwipeButtonView;
import com.trava.user.utils.swipeButton.View.RightSwipeButtonView;

/**
 * Created by Gratus on 02/10/18.
 */

public interface OnSwipeCompleteListener {
    void onSwipe_Forward(LeftSwipeButtonView swipe_button_view);
    void onSwipe_RejectForward(RightSwipeButtonView swipe_button_view);
    void onSwipe_Reverse(LeftSwipeButtonView swipe_button_view);
    void onSwipe_RejectReverse(RightSwipeButtonView swipe_button_view);
}
