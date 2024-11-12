package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetCompanyPostedJobByIdRes(
    val data: CompanyJobData,
    val message: String
)

data class CompanyJobData(
    val address: String,
    val age_requirement: Int,
    val bookmark_status: Int,
    val category_name: String,
    val company_address: String,
    val company_job_url: String,
    val company_name: String,
    val company_zip_code: String,
    val description: String,
    val distance_in_miles: String,
    val experience: String,
    val images: List<Image>,
    val is_reviewed: Int,
    val job_title: String,
    val job_type: Int,
    val job_types: List<Int>,
    val lat: Double,
    val lon: Double,
    val posted_by: Int,
    val profile_image_url: String,
    val salary: String,
    val status_id: Any,
    val total_applicant: Int,
    val user_type: Int,
    val zip_code: String,
    @SerializedName("job_id")
    val jobId: Int?= null,
)