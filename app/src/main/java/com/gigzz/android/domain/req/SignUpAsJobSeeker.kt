package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class SignUpAsJobSeeker(
    @SerializedName("email_id")
    var emailId: String? = null,
    var password: String? = null,
    @SerializedName("full_name")
    var fullName: String? = null,
    @SerializedName("profile_thumbnail")
    var profileThumbnail: String? = "",
    @SerializedName("phone_number")
    var phoneNo: String? = null,
    var gender: String? = null,
    @SerializedName("age_group")
    var ageGroup: Int? = null,
    @SerializedName("parent_email_id")
    var parentEmail: String? = "",
    var bio: String? = null,
    var dob: String? = null,
    @SerializedName("interests")
    var interests: List<String> = emptyList(),
    @SerializedName("skills")
    var skills: List<String> = emptyList(),
    @SerializedName("work_permit_url")
    var workPermitUrl: String? = null,
    @SerializedName("resume_url")
    var resumeUrl: String? = null,
    @SerializedName("profile_image_url")
    var profileImageUrl: String? = "",
    var address: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    @SerializedName("zip_code")
    var zipCode: String? = null,
    @SerializedName("country_short_code")
    var countryShortCode: String?="",
    @SerializedName("country_code")
    var countryCode: String? = "",
    var city: String?= "",
    var state: String?=""
)
