package com.gigzz.android

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gigzz.android.common.NetworkStatus
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.netConnectUtils.NetConnectivityHelper
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp:Application() {

    private lateinit var broadcaster: LocalBroadcastManager
    private val networkConnectionHelper: MutableLiveData<NetworkStatus> by lazy {
        NetConnectivityHelper(
            this
        )
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
       // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        /*PaymentConfiguration.init(
            applicationContext,
            resources.getString(R.string.publish_key)
        )*/
        broadcaster = LocalBroadcastManager.getInstance(this)

        observeInternetConnection()
    }

    private fun observeInternetConnection() {
        networkConnectionHelper.observeForever {
            when (it) {
                NetworkStatus.Available -> broadcaster.sendBroadcast(Intent(AppConstants.NET_AVAILABLE))
                NetworkStatus.Unavailable -> broadcaster.sendBroadcast(Intent(AppConstants.NET_UNAVAILABLE))
                else -> {}
            }
        }
    }
}