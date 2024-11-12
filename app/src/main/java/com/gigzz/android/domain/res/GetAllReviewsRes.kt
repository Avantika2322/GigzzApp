package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetAllReviewsRes( var message: String? = null,
                             var data: ArrayList<ReviewsData>? = arrayListOf()
)

data class ReviewsData(
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("user_type") var userType: Int? = null,
    @SerializedName("user_full_name") var userFullName: String? = null,
    @SerializedName("user_profile_image_url") var userProfileImageUrl: String? = null,
    @SerializedName("company_name") var companyName: String? = null,
    @SerializedName("company_profile_image_url") var companyProfileImageUrl: String? = null,
    @SerializedName("individual_full_name") var individualFullName: String? = null,
    @SerializedName("individual_profile_image_url") var individualProfileImageUrl: String? = null,
    @SerializedName("rating") var rating: Int? = null,
    @SerializedName("review") var review: String? = null,
    @SerializedName("created_datetime") var createdDatetime: String? = null
)