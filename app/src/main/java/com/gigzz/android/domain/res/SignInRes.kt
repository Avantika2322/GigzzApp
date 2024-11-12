package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

data class SignInRes(
    val data: SignInData? = null,
    val message: String?
)


@Serializable
data class SignInData(
    @SerializedName("address")
    val address: String?,
    @SerializedName("age_group")
    val ageGroup: Int?,
    @SerializedName("auth_key")
    val authKey: String?,
    @SerializedName("average_rating")
    val averageRating: Int?,
    val description: String?,
    val bio: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    val dob: String?,
    @SerializedName("email_id")
    val emailId: String?,
    @SerializedName("full_name")
    val fullName: String?,
    val gender: String?,
    val interest: List<String>?,
    val opportunities: List<String>?,
    val lat: Double?,
    val lon: Double?,
    @SerializedName("parent_email_id")
    val parentEmailId: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    @SerializedName("profile_thumbnail")
    val profileThumbnail: String?,
    @SerializedName("resume_url")
    val resumeUrl: String?,
    val skills: List<String>?,
    @SerializedName("total_connection")
    val totalConnection: Int?,
    @SerializedName("total_review_rating")
    val totalReviewRating: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("user_type")
    val userType: Int?,
    @SerializedName("work_permit_url")
    val workPermitUrl: String?,
    @SerializedName("zip_code")
    val zipCode: String?,
    @SerializedName("company_name") var companyName: String? = "",
    @SerializedName("hiring_name") var hiringName: String = "",
    @SerializedName("company_logo_url") var companyLogoUrl: String? = null,
    var cachedCompanyLogoUrl: String? = null,
    @SerializedName("company_thumbnail") var companyThumbnail: String? = null,
    @SerializedName("job_title") var jobTitle: String = "",
    @SerializedName("job_description") var jobDescription: String = "",
    @SerializedName("required_experience") var requiredExperience: String = "",
    @SerializedName("industry_type") var industryType: List<String> = emptyList(),
    var cacheProfileImage: String = "",
) {
    companion object {
        val default = SignInData(
            address = "",
            ageGroup = -1,
            authKey = "",
            averageRating = -1,
            description = "",
            countryCode = "",
            dob = "",
            emailId = "",
            fullName = "",
            gender = "",
            interest = persistentListOf(),
            lat = 0.0,
            lon = 0.0,
            parentEmailId = "",
            phoneNumber = "",
            profileImageUrl = "",
            profileThumbnail = "",
            companyLogoUrl="",
            resumeUrl = "",
            skills = persistentListOf(),
            totalConnection = -1,
            userType = 1,
            workPermitUrl = "",
            zipCode = "",
            totalReviewRating = -1,
            opportunities = persistentListOf(),
            bio = "",
            userId = -1
        )
    }
}