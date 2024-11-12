package com.gigzz.android.utils

import android.content.pm.PackageManager
import android.os.Build
import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.domain.req.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeviceInfoModule @Inject constructor() {

    fun getDeviceInfo(prefs: PreferenceDataStoreModule): Flow<Resource<DeviceInfo>> = flow {
        val response = try {
            val deviceToken = prefs.getFirstPreference(PrefKeys.DEVICE_TOKEN,"")
            val timeOffset = getTimeOffset()
            val deviceModel = Build.MODEL
            val osVersion = Build.VERSION.SDK_INT
            val appVersion = getVersionName(prefs)
            val info = DeviceInfo.default.copy(
                deviceToken = deviceToken,
                timeOffSet = timeOffset,
                makeModel = deviceModel,
                osVersion = osVersion.toString(),
                appVersion = appVersion.toString()
            )
            Resource.Success(info)
        } catch (ex: Exception) {
            Resource.Error(ex.message.toString())
        }
        emit(response)
    }

    private fun getVersionName(prefs: PreferenceDataStoreModule): String {
        try {
            val packageManager = prefs.appCtx.packageManager
            val packageName = prefs.appCtx.packageName
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}