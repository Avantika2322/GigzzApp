package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class SignUpAsIndividualJobGiverReq(
    @SerializedName("email_id")
    var emailId: String? = null,
    @SerializedName("password")
    var password: String? = null,
    @SerializedName("full_name")
    var fullName: String? = null,
    @SerializedName("phone_number")
    var phoneNo: String? = null,
    @SerializedName("opportunities")
    var opportunities: List<String> = emptyList(),
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("lat")
    var lat: String? = null,
    @SerializedName("lon")
    var lon: String? = null,
    @SerializedName("zip_code")
    var zipCode: String? = null,
    @SerializedName("country_code")
    var countryCode: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("city")
    var city: String?= "",
    @SerializedName("state")
    var state: String?=""
)
