package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class AddCommentOnPostReq(
    val comment: String,
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("tagged_users")
    val taggedUsers: ArrayList<Int> = arrayListOf()
)