package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetSettingsResponse (
    var message : String? = null,
    var data : List<SettingData> = emptyList())

data class SettingData (
    @SerializedName("setting_key") var settingKey : String? = null,
    @SerializedName("setting_value") var settingValue : String? = null,
    @SerializedName("is_active") var isActive : Int? = null
)
