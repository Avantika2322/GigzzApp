package com.gigzz.android.domain.res

data class SendOtpRes(
    var data: Data? = null,
    val message: String?
)

data class Data(val otp: String)