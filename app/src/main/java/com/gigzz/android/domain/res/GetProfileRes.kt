package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetProfileRes(
    var data: ProfileData? = null,
    var message: String? = null,
)

data class ProfileData(
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("email_id") var emailId: String? = null,
    @SerializedName("user_type") var userType: Int? = null,
    @SerializedName("total_review_rating") var totalReviewRating: Int? = null,
    @SerializedName("average_rating") var averageRating: Int? = null,
    @SerializedName("overall_rating") var overAllRating: String? = null,
    @SerializedName("total_connection") var totalConnection: Int? = null,
    @SerializedName("auth_key") var authKey: String? = null,
    @SerializedName("full_name") var fullName: String? = null,
    @SerializedName("country_code") var countryCode: String? = null,
    @SerializedName("phone_number") var phoneNumber: String? = null,
    var gender: String? = null,
    @SerializedName("age_group") var ageGroup: Int? = null,
    @SerializedName("parent_email_id") var parentEmailId: String? = null,
    var bio: String? = null,
    var dob: String? = null,
    var interest: ArrayList<String> = arrayListOf(),
    var skills: ArrayList<String> = arrayListOf(),
    @SerializedName("work_permit_url") var workPermitUrl: String? = null,
    @SerializedName("resume_url") var resumeUrl: String? = null,
    @SerializedName("profile_image_url") var profileImageUrl: String? = null,
    var cachedProfileImageUrl: String? = null,
    @SerializedName("profile_thumbnail") var profileThumbnail: String? = null,
    var cached_profile_pic_thumbnail: String? = "",
    var address: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    @SerializedName("zip_code") var zipCode: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("opportunities") var opportunities: List<String> = emptyList(),
    @SerializedName("company_name") var companyName: String? = null,
    @SerializedName("hiring_name") var hiringName: String? = null,
    @SerializedName("job_title") var jobTitle: String? = null,
    @SerializedName("job_description") var jobDescription: String? = null,
    @SerializedName("required_experience") var requiredExperience: String? = null,
    @SerializedName("industry_type") var industryType: ArrayList<String> = arrayListOf(),
)