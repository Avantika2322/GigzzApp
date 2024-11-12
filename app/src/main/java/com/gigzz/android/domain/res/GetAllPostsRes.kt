package com.gigzz.android.domain.res

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GetAllPostsRes(
    var data: List<PostData>?,
    var message: String?
)

@Parcelize
data class PostData(
    var caption: String?,
    @SerializedName("created_datetime")
    var createdDatetime: String?,
    @SerializedName("is_connecting")
    var isConnecting: Int?,
    @SerializedName("is_liked")
    var isLiked: Int?,
    @SerializedName("media_item")
    var mediaItem: List<MediaItem?>?,
    @SerializedName("post_id")
    var postId: Int?,
    @SerializedName("post_type")
    var postType: String?,
    @SerializedName("posted_by_user")
    var postedByUser: String?,
    @SerializedName("total_comments_count")
    var totalCommentsCount: Int?,
    @SerializedName("total_likes_count")
    var totalLikesCount: Int?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("user_profile_image_url")
    var userProfileImageUrl: String?,
    @SerializedName("user_type")
    var userType: Int?,
    var privacy: Int? = null,
    var cacheImage: String? = null,
//Image data for job Detail
    @SerializedName("image_thumbnail") val imageThumbnail: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("media_id") val mediaId: Int?,
    @SerializedName("job_details") val jobDetails: JobDetails? = null,
) : Parcelable

@Parcelize
data class MediaItem(
    @SerializedName("height_in_pixels")
    var heightInPixels: String?,
    @SerializedName("media_id")
    var mediaId: Int?,
    @SerializedName("media_thumbnail")
    var mediaThumbnail: String?,
    @SerializedName("media_thumbnail_100_x_100")
    var mediaThumbnail100X100: String?,
    @SerializedName("media_type")
    var mediaType: String?,
    @SerializedName("media_url")
    var mediaUrl: String?,
    @SerializedName("width_in_pixels")
    var widthInPixels: String?,
    var cacheMediaUrl: String?=null,
    @SerializedName("image_thumbnail") val imageThumbnail: String?,
    @SerializedName("image_url") val imageUrl: String?,
    var cachedImage:String?=null
) : Parcelable


@Parcelize
data class JobDetails(
    val address: String?,
    @SerializedName("age_requirement")
    val ageRequirement: Int?,
    @SerializedName("bookmark_status")
    val bookmarkStatus: Int?,
    @SerializedName("company_address")
    val companyAddress: String?,
    @SerializedName("company_category_name")
    val companyCategoryName: String?,
    @SerializedName("company_name")
    val companyName: String?,
    val compensation: String?,
    val description: String?,
    @SerializedName("end_date")
    val endDate: String?,
    @SerializedName("end_time")
    val endTime: String?,
    val experience: String?,
    val images: List<Image>?,
    @SerializedName("individual_category_name")
    val individualCategoryName: String?,
    @SerializedName("individual_job_url")
    val individualJobUrl: String?,
    @SerializedName("individual_jobs_description")
    val individualJobsDescription: String?,
    @SerializedName("individual_name")
    val individualName: String?,
    @SerializedName("job_id")
    val jobId: Int?,
    @SerializedName("job_location")
    val jobLocation: Int?,
    @SerializedName("job_name")
    val jobName: String?,
    @SerializedName("job_title")
    val jobTitle: String?,
    @SerializedName("job_type")
    val jobType: Int?,
    @SerializedName("job_types")
    val jobTypes: List<Int>?,
    @SerializedName("posted_by")
    val postedBy: Int?,
    val salary: String?,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("status_id")
    val statusId: Int?,
    @SerializedName("total_hours")
    val totalHours: String?
) : Parcelable
