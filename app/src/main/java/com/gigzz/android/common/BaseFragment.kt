package com.gigzz.android.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gigzz.android.utils.AppConstants

abstract class BaseFragment: Fragment() {

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(AppConstants.NET_AVAILABLE)
            addAction(AppConstants.NET_UNAVAILABLE)
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(networkStatusReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(networkStatusReceiver)
    }

    private var networkStatusReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                AppConstants.NET_AVAILABLE -> {
                    onNetworkStatusChangedToAvailable()
                }
                AppConstants.NET_UNAVAILABLE -> {
                    onNetworkUnavailable()
                }
            }
        }
    }

    abstract fun onNetworkStatusChangedToAvailable()

    abstract fun onNetworkUnavailable()
}