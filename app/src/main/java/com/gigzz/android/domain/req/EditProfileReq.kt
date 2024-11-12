package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class EditProfileReq(
    @SerializedName("country_code") var countryCode: String? = null,
    @SerializedName("phone_number") var phoneNumber: String? = null,
    var bio: String? = null,
    var address: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    @SerializedName("zip_code") var zipCode: String? = null,
    @SerializedName("profile_image_url") var profileImage: String? = null,
    @SerializedName("resume_url") var resumeUrl: String? = null
)