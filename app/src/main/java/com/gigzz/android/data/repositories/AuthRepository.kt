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

class AuthRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val apiAuthInterface: ApiAuthInterface
) {
    suspend fun getMasterDataApi(): Response<GetMasterDataRes> =
        apiInterface.saveMasterData()

    suspend fun demoDataApi(): Response<Demo> =
        apiInterface.demoData()

    suspend fun sendOtpApi(emailId: String): Response<SendOtpRes> =
        apiInterface.sendOtpForSingUp(emailId)

    suspend fun verifyOtpApi(emailId: String, otp: String): Response<GeneralResponse> =
        apiInterface.verifyOtpForSignUp(emailId,otp)

    suspend fun signUpAsJobSeekerApi(signUpAsJobSeeker: SignUpAsJobSeeker): Response<SignInRes> =
        apiInterface.signUpAsCandidate(signUpAsJobSeeker)

    suspend fun signUpAsIndividualApi(signUpAsIndividualJobGiverReq: SignUpAsIndividualJobGiverReq): Response<SignInRes> =
        apiInterface.signUpAsIndividualJobGiver(signUpAsIndividualJobGiverReq)

    suspend fun signUpAsCompanyApi(signUpAsCompanyReq: SignUpAsCompanyReq): Response<SignInRes> =
        apiInterface.signUpAsCompany(signUpAsCompanyReq)

    suspend fun signInApi(emailId: String, password: String): Response<SignInRes> =
        apiInterface.userSignIn(emailId,password)

    suspend fun updateDeviceInfoApi(deviceInfo: DeviceInfo): Response<GeneralResponse> =
        apiAuthInterface.updateDeviceToken(deviceInfo)

    suspend fun addEditEducationApi(addEditEducationReq: AddEditEducationReq): Response<GeneralResponse> =
        apiAuthInterface.addUserEducationDetails(addEditEducationReq)
}