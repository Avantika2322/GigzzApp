package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetNotificationsRes(
    val data: ArrayList<NotificationData>,
    val message: String,
    val status: Int
)

data class NotificationData(
    @SerializedName("created_datetime")
    val createdDatetime: String,
    val id: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("is_connecting")
    val isConnecting: Int,
    @SerializedName("notification_id")
    val notificationId: Int,
    @SerializedName("notification_type")
    val notificationType: Int,
    @SerializedName("other_user_id")
    val otherUserId: Int,
    val text: String,
    @SerializedName("updated_datetime")
    val updatedDatetime: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_type")
    val userType: Int
)