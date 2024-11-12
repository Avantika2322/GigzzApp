package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("device_token")
    val deviceToken: String,
    @SerializedName("time_offset")
    val timeOffSet: String,
    @SerializedName("device_make_model")
    val makeModel: String,
    @SerializedName("device_os_version")
    val osVersion: String,
    @SerializedName("installed_app_version")
    val appVersion: String,
    @SerializedName("device_type")
    val deviceType: String
){
    companion object {
        val default = DeviceInfo(
            deviceToken = "",
            timeOffSet = "",
            makeModel = "",
            osVersion = "",
            appVersion = "1.0.0",
            deviceType = "A"
        )
    }
}
