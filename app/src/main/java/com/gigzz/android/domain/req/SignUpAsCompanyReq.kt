package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class SignUpAsCompanyReq(
    @SerializedName("email_id")
    var emailId: String? = null,
    var password: String? = null,
    var address: String? = null,
    @SerializedName("company_name")
    var companyName: String? = null,
    @SerializedName("hiring_name")
    var hiringName: String? = null,
    @SerializedName("profile_image_url")
    var profileImageUrl: String? = null,
    @SerializedName("profile_thumbnail")
    var profileThumbnail: String? = null,
    @SerializedName("industry_type")
    var industryType: List<String> = emptyList(),
    @SerializedName("job_description")
    var jobDescription: String? = null,
    @SerializedName("job_title")
    var jobTitle: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    @SerializedName("required_experience")
    var requiredExperience: String? = null,
    @SerializedName("zip_code")
    var zipCode: String? = null,
    @SerializedName("city")
    var city: String?= "",
    @SerializedName("state")
    var state: String?=""
)
