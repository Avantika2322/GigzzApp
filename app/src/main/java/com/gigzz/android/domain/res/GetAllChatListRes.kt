package com.gigzz.android.domain.res

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class GetAllChatListRes(
    @SerializedName("data")
    val threads: List<ChatListData>? = null,
    val message: String,
    val status: Int,
)

@Parcelize
data class ChatListData(
    @SerializedName("chat_id")
    val chatId: Int,
    @SerializedName("created_datetime")
    val createdDatetime: String?,
    @SerializedName("first_user_id")
    val firstUserId: Int,
    @SerializedName("is_blocked")
    val isBlocked: Int,
    @SerializedName("last_message")
    val lastMessage: String?,
    @SerializedName("last_message_created_at")
    val lastMessageCreatedAt: String?,
    @SerializedName("other_blocked_you")
    val otherBlockedYou: Int,
    @SerializedName("posted_by_user")
    val otherUser: String?,
    @SerializedName("second_user_id")
    val secondUserId: Int,
    @SerializedName("total_message")
    val totalMessage: Int,
    @SerializedName("user_profile_image_url")
    val userProfileImageUrl: String?,
    @SerializedName("you_blocked_other")
    val youBlockedOther: Int,
    @SerializedName("user_type") var userType: Int? = null,
    @SerializedName("is_connecting") var isConnecting: Int? = null,
    var cachedImage: String? = "",
    var isFrom: String? = "",
) : Parcelable
