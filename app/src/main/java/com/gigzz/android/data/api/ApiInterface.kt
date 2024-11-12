package com.gigzz.android.data.api

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.domain.Demo
import com.gigzz.android.domain.req.DeviceInfo
import com.gigzz.android.domain.req.SignUpAsCompanyReq
import com.gigzz.android.domain.req.SignUpAsIndividualJobGiverReq
import com.gigzz.android.domain.req.SignUpAsJobSeeker
import com.gigzz.android.domain.res.GetMasterDataRes
import com.gigzz.android.domain.res.GetSettingsResponse
import com.gigzz.android.domain.res.MasterData
import com.gigzz.android.domain.res.SendOtpRes
import com.gigzz.android.domain.res.SignInRes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {
    @GET("get-masters-data")
    suspend fun saveMasterData(): Response<GetMasterDataRes>

    @GET("launches")
    suspend fun demoData(): Response<Demo>

    @GET("get-app-settings")
    suspend fun getAppSettingData(): Response<GetSettingsResponse>

    @POST("user-sign-in")
    @FormUrlEncoded
    suspend fun userSignIn(@Field("email_id") email: String, @Field("password") password: String): Response<SignInRes>

    @FormUrlEncoded
    @POST("send-email-verification-code")
    suspend fun sendOtpForSingUp(@Field("email_id") email: String): Response<SendOtpRes>

    @FormUrlEncoded
    @POST("verify_email_verification-code")
    suspend fun verifyOtpForSignUp(@Field("email_id") email: String, @Field("otp") otp: String): Response<GeneralResponse>

    @POST("sign-up-as-candidate")
    suspend fun signUpAsCandidate(@Body signUpModel: SignUpAsJobSeeker): Response<SignInRes>

    @POST("sign-up-as-individual-job-giver")
    suspend fun signUpAsIndividualJobGiver(@Body signUpAsIndividualJobGiver: SignUpAsIndividualJobGiverReq): Response<SignInRes>

    @POST("sign-up-as-company")
    suspend fun signUpAsCompany(@Body signUpAsCompanyModel: SignUpAsCompanyReq): Response<SignInRes>
}