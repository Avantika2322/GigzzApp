package com.gigzz.android.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gigzz.android.utils.AppConstants

abstract class BaseActivity: AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(AppConstants.NET_AVAILABLE)
            addAction(AppConstants.NET_UNAVAILABLE)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(networkStatusReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkStatusReceiver)
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