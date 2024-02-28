package com.codebrew.clikat.app_utils.extension

import android.graphics.Color
import android.view.View
import com.codebrew.clikat.R
import com.google.android.material.snackbar.Snackbar


fun View.onSnackbar(msg:String){
    //Snackbar(view)
    val snackbar = Snackbar.make(this, msg,
            Snackbar.LENGTH_LONG)

    snackbar.setAction(R.string.ok) { snackbar.dismiss() }
    val snackbarView = snackbar.view
    snackbarView.setBackgroundColor(resources.getColor(R.color.white))
    snackbar.setActionTextColor(resources.getColor(R.color.black))
    snackbar.setTextColor(resources.getColor(R.color.black))
    snackbar.show()
}