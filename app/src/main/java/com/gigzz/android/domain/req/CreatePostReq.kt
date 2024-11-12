package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName
import java.io.File

data class CreatePostReq(
    var caption: String? = null,
    @SerializedName("post_type") var postType: String? = null,
    @SerializedName("tagged_users") var taggedUsers:ArrayList<Int> = arrayListOf(),
    var medias: ArrayList<Media> = arrayListOf(),
    var privacy: Int? = null,
    @SerializedName("post_id") var postId:Int?= null
)

data class Medias(
    @SerializedName("media_type") var mediaType: String? = null,
    @SerializedName("media_url") var mediaUrl: File? = null,
    @SerializedName("media_thumbnail") var mediaThumbnail: File? = null,
    @SerializedName("media_thumbnail_100_x_100") var mediaThumbnail100X100: String? = null,
    @SerializedName("width_in_pixels") var widthInPixels: String? = null,
    @SerializedName("height_in_pixels") var heightInPixels: String? = null,
)

data class Media(
    @SerializedName("media_type") var mediaType: String? = null,
    @SerializedName("media_url") var mediaUrl: String? = null,
    @SerializedName("media_thumbnail") var mediaThumbnail: String? = null,
    @SerializedName("media_thumbnail_100_x_100") var mediaThumbnail100X100: String? = null,
    @SerializedName("width_in_pixels") var widthInPixels: String? = null,
    @SerializedName("height_in_pixels") var heightInPixels: String? = null
)