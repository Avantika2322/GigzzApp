package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetAllCommentByIdRes(
    val data: ArrayList<CommentData>,
    val message: String
)

data class CommentData(
    val comment: String,
    @SerializedName("comment_by")
    val commentBy: Int,
    @SerializedName("comment_by_user")
    val commentByUser: String,
    @SerializedName("comment_id")
    val commentId: Int,
    @SerializedName("created_datetime")
    val createdDatetime: String,
    @SerializedName("is_edited")
    val isEdited: Int,
    @SerializedName("tagged_users")
    val taggedUsers: Any?,
    @SerializedName("user_profile_image_url")
    val userProfileImageUrl: String
)

data class TaggedUser(
    @SerializedName("user_id")
    val userId: Int
)