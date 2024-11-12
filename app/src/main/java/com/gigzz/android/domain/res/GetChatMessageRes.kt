package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetChatMessageRes(
    val message: String,
    val data: ArrayList<GetChatMessage>? = null,
)

data class GetChatMessage(
    @SerializedName("chat_id")
    val chatId: Int,
    @SerializedName("created_at")
    var createdAt: String,
    @SerializedName("media_url")
    val mediaUrl: String?=null,
    val message: String,
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("message_send_by")
    val messageSendBy: String,
    @SerializedName("msg_type")
    val msgType: String,
    @SerializedName("posted_by_user")
    val postedBy: String,
    @SerializedName("receiver_id")
    val receiverId: String,
    @SerializedName("receiver_read_flag")
    val receiverReadFlag: Int,
    @SerializedName("sender_id")
    val senderId: String,
    @SerializedName("sender_read_flag")
    val senderReadFlag: Int,
    @SerializedName("user_profile_image_url")
    val profileImageUrl: String?=null,
    @SerializedName("is_connecting") var isConnecting: Int? = null,
    @SerializedName("user_type") var userType: Int? = null
)
