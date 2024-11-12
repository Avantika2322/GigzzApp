package com.gigzz.android.domain.res

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GetJobsRes(
    val data: ArrayList<JobsData>,
    val message: String
)

@Parcelize
data class JobsData(
    val address: String?,
    @SerializedName("age_requirement")
    val ageRequirement: Int?,
    @SerializedName("bookmark_status")
    val bookmarkStatus: Int?,
    @SerializedName("company_address")
    val companyAddress: String?,
    @SerializedName("company_category_name")
    val companyCategoryName: String?,
    @SerializedName("company_job_url")
    val companyJobUrl: String?,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("company_zip_code")
    val companyZipCode: String?,
    val compensation: String?,
    @SerializedName("created_datetime")
    val createdDatetime: String?,
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
    val jobId: Int,
    @SerializedName("job_name")
    val jobName: String?,
    @SerializedName("job_title")
    val jobTitle: String?,
    @SerializedName("job_type")
    val jobType: Int?,
    @SerializedName("job_types")
    val jobTypes: List<Int>?,
    val lat: Double,
    val lon: Double,
    @SerializedName("posted_by")
    val postedBy: Int,
    val salary: String?,
    @SerializedName("start_date")
    val startDate: String? = null,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("status_id")
    val statusId: Int?,
    @SerializedName("total_hours")
    val totalHours: String?,
    @SerializedName("job_status")
    var jobStatus: String? = null,
    @SerializedName("user_profile_image_url")
    val userProfileImageUrl: String?,
    var cachedUserProfileImageUrl: String? = null,
    @SerializedName("user_type")
    val userType: Int?
) : Parcelable

@Parcelize
data class Image(
    @SerializedName("image_thumbnail")
    val imageThumbnail: String?,
    var cachedImgThumbnailUrl: String? = null,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("media_id")
    val mediaId: String?
): Parcelable
