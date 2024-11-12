package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class RatingReqModel(
    @SerializedName("review_to") var reviewTo: Int? = null,
    @SerializedName("rating") var rating: Float? = null,
    @SerializedName("review") var review: String? = null,
    @SerializedName("other_user_id") var otherUserId: Int? = null

)
