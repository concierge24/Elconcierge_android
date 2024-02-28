package com.trava.utilities

import android.os.Bundle
import android.view.View
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.net.ConnectivityManager
import android.content.IntentFilter
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment() {

    abstract fun onNetworkConnected()

    abstract fun onNetworkDisconnected()

    private val networkChangeReceiver = NetworkChangeReceiver()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        activity?.registerReceiver(networkChangeReceiver, intentFilter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(networkChangeReceiver)
    }

    inner class NetworkChangeReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (CheckNetworkConnection.isOnline(context)) {
                onNetworkConnected()
            } else {
                onNetworkDisconnected()
            }

        }
    }

}