package com.gigzz.android.data.repositories

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.data.api.ApiInterface
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.domain.Demo
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.domain.req.DeviceInfo
import com.gigzz.android.domain.req.SignUpAsCompanyReq
import com.gigzz.android.domain.req.SignUpAsIndividualJobGiverReq
import com.gigzz.android.domain.req.SignUpAsJobSeeker
import com.gigzz.android.domain.res.GetMasterDataRes
import com.gigzz.android.domain.res.GetSettingsResponse
import com.gigzz.android.domain.res.MasterData
import com.gigzz.android.domain.res.SendOtpRes
import com.gigzz.android.domain.res.SignInRes
import com.gigzz.android.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val apiAuthInterface: ApiAuthInterface
) {

}