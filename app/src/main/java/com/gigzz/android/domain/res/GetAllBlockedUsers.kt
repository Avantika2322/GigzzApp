package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetAllBlockedUsers(
    var message: String?,
    var data: ArrayList<BlockedUSerData>? = arrayListOf()
)

data class BlockedUSerData(
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("user_name")
    var userName: String?,
    @SerializedName("user_profile_image_url")
    var userProfileImageUrl: String?,
    var cacheImage:String?
)