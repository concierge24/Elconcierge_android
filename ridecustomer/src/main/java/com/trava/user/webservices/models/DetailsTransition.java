package com.trava.user.webservices.models;


import androidx.transition.ChangeBounds;
import androidx.transition.TransitionSet;

public class DetailsTransition extends TransitionSet {
    public DetailsTransition() {
        addTransition(new ChangeBounds());
    }
}