package com.gigzz.android.common

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
}
