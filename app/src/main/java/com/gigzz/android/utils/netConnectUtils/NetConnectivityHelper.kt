package com.gigzz.android.utils.netConnectUtils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.fastrack.android.utils.netConnectUtils.DoesNetworkHaveInternet
import com.gigzz.android.common.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetConnectivityHelper(appContext: Context): MutableLiveData<NetworkStatus>() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = appContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private fun checkValidNetworks() {
        if (validNetworks.size > 0) {
            postValue(NetworkStatus.Available)
        } else {
            postValue(NetworkStatus.Unavailable)
        }
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        /*
          Called when a network is detected. If that network has internet, save it in the Set.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
         */
        @SuppressLint("MissingPermission")
        override fun onAvailable(network: Network) {
            Log.d("Internet", "onAvailable: $network")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Log.d("Internet", "onAvailable: ${network}, $hasInternetCapability")

            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.Main) {
                            Log.d("Internet", "onAvailable: adding network. $network")
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         */
        @SuppressLint("MissingPermission")
        override fun onLost(network: Network) {
            Log.d("Internet", "onLost: $network")

            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasTransport(TRANSPORT_CELLULAR)
            if(hasInternetCapability == false) validNetworks.remove(network)
            checkValidNetworks()
        }

    }


    @SuppressLint("MissingPermission")
    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        cm.unregisterNetworkCallback(networkCallback)
    }


}