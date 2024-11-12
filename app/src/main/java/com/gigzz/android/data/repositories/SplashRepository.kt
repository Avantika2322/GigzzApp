package com.gigzz.android.data.repositories

import com.gigzz.android.data.api.ApiInterface
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.domain.res.GetSettingsResponse
import retrofit2.Response
import javax.inject.Inject

class SplashRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val preferenceModule: PreferenceDataStoreModule
) {

    suspend fun getSettingsApi(): Response<GetSettingsResponse> =
        apiInterface.getAppSettingData()
}