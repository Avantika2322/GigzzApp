package com.gigzz.android.domain.res

import com.gigzz.android.domain.req.Media
import com.google.gson.annotations.SerializedName

data class GetPostedJobByIdRes(
    val data: JobDataById,
    val message: String
)

data class JobDataById(
    val address: String?=null,
    @SerializedName("bookmark_status")
    val bookmarkStatus: Int?=null,
    @SerializedName("category_name")
    val categoryName: String?=null,
    val compensation: String?=null,
    val description: String?=null,
    @SerializedName("end_date")
    val endDate: String?=null,
    @SerializedName("end_time")
    val endTime: String?=null,
    @SerializedName("full_name")
    val fullName: String?=null,
    @SerializedName("individual_job_url")
    val individualJobUrl: String?,
    @SerializedName("is_reviewed")
    val isReviewed: Int?=null,
    @SerializedName("job_name")
    val jobName: String?=null,
    @SerializedName("job_type")
    val jobType: Int?=null,
    val lat: Double?=null,
    val lon: Double?=null,
    @SerializedName("posted_by")
    val postedBy: Int?=null,
    @SerializedName("profile_image_url")
    val profileImageUrl:String? = null,
    @SerializedName("start_date")
    val startDate: String?=null,
    @SerializedName("start_time")
    val startTime: String?=null,
    @SerializedName("status_id")
    val statusId: Int?=null,
    @SerializedName("total_applicant")
    val totalApplicant: Int?=null,
    @SerializedName("total_hours")
    val totalHours: String?=null,
    @SerializedName("user_type")
    val userType: Int?=null,
    @SerializedName("zip_code")
    val zipCode: String?=null,
    val images: ArrayList<Image>? = arrayListOf(),
    @SerializedName("user_id") var userId: Int? = null,
    var name: String? = null,
    @SerializedName("job_id") var jobId: Int? = null,
)
