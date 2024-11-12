package com.gigzz.android.domain.res

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class GetAllJobsRes(
    val data: List<JobData>? = null,
    val message: String? = null,
)

@Parcelize
data class JobData(
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("category_name") var categoryName: String? = null,
    var name: String? = null,
    var description: String? = null,
    /*var images: List<Image>? = emptyList(),*/
    var images: List<MediaItem>? = emptyList(),
//searchedJobs
    @SerializedName("zip_code") var zipCode: String? = null,
    @SerializedName("job_id") var jobId: Int? = null,
    @SerializedName("posted_by") var postedBy: Int? = null,
    @SerializedName("job_type") var jobType: Int? = null,
    @SerializedName("job_name") var jobName: String? = null,
    @SerializedName("total_hours") var totalHours: String? = null,
    @SerializedName("start_date") var startDate: String? = null,
    @SerializedName("end_date") var endDate: String? = null,
    @SerializedName("start_time") var startTime: String? = null,
    @SerializedName("end_time") var endTime: String? = null,
    var compensation: String? = null,
    @SerializedName("company_name") var companyName: String? = null,
    @SerializedName("profile_image_url") var profileImageUrl: String? = null,
    var cachedProfileImageUrl: String? = null,
    @SerializedName("job_title") var jobTitle: String? = null,
    @SerializedName("job_types") var jobTypes: Int? = null,
    @SerializedName("age_requirement") var ageRequirement: Int? = null,
    var experience: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    var address: String? = null,
    var salary: String? = null,
    @SerializedName("company_address") var companyAddress: String? = null,
    @SerializedName("company_zip_code") var companyZipCode: String? = null,
    @SerializedName("user_type") var userType: Int? = null,
    //companyJobFilter
    @SerializedName("status_id") var statusId: String? = null,
    @SerializedName("total_applicant") var totalApplicant: String? = null,
    @SerializedName("individual_jobs_description") var individualJobDes: String? = null,
    @SerializedName("individual_category_name") var individualCategoryName: String? = null,
    @SerializedName("is_reviewed") var alreadyReviewed: Int? = null,
    @SerializedName("company_category_name") var companyCategoryName: String? = null,
    @SerializedName("job_status") var jobStatus: String? = null,
    @SerializedName("created_datetime") var createdDatetime: String? = null,
    @SerializedName("company_job_url") var companyJobUrl: String?,
    @SerializedName("individual_job_url") var individualJobUrl: String?,
    @SerializedName("user_profile_image_url") var companyLogo: String?,
    var cacheImage: String? = null,
    @SerializedName("bookmark_status") var bookmarkStatus: Int? = null,
    var edit: Boolean = false
) : Parcelable