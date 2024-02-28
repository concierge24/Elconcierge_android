package com.trava.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.view.View

/**
 * This if a utility class to check the internet connectivity of the device
 * and to show the internet connectivity error as a SnackBar
 * */

object CheckNetworkConnection {

    /**
     * Check the internet connectivity of the device
     * @param context context to get system services
     * @return true if the internet connection is available, otherwise false
     * */
    fun isOnline(context: Context?): Boolean {
        if (context != null) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }
        return true
    }

    /**
     * Shows the internet connectivity error using SnackBar
     * @param view The view to find a parent from, for the SnackBar
     * */
    fun showNetworkError(view: View) {
        view.showSnack(R.string.network_error)
    }
}
