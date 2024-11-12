package com.gigzz.android.domain.res

data class AllConnectionRes(
    val data: ArrayList<ConnectionData>,
    val message: String
)

data class ConnectionData(
    val is_connecting: Int?,
    val user_id: Int,
    val user_name: String,
    val user_profile_image_url: String?,
    var cached_user_profile_image_url: String? = null,
    val user_type: Int?
)