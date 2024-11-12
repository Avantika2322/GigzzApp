package com.gigzz.android.domain.res

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class CreateChatRes(
    val data: CreateData? = null,
    val message: String? = null,
    val status: Int? = null,
    val statusCode: Int? = null,
)

@Parcelize
data class CreateData(
    @SerializedName("chat_id")
    val chatId: Int,
    @SerializedName("chat_type")
    var chatType: Int? = null,
    @SerializedName("first_user_id")
    val firstUserId: Int,
    @SerializedName("second_user_id")
    val secondUserId: Int,
    @SerializedName("is_connecting") var isConnecting: Int? = null,
    @SerializedName("you_blocked_other") var blockedByYou: Int? = null,
    @SerializedName("other_blocked_you") var blockedByOther: Int? = null,
) : Parcelable
