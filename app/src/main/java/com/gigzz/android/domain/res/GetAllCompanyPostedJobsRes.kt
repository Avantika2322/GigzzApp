package com.gigzz.android.domain.res

data class GetAllCompanyPostedJobsRes(
    val data: List<CompanyJobData>,
    val message: String
)

/*
data class Data(
    val address: String,
    val age_requirement: Int,
    val category_name: String,
    val company_address: String,
    val company_name: String,
    val company_zip_code: String,
    val created_datetime: String,
    val description: String,
    val experience: String,
    val images: List<Any>,
    val job_id: Int,
    val job_title: String,
    val job_type: Int,
    val job_types: List<Int>,
    val lat: Double,
    val lon: Double,
    val posted_by: Int,
    val salary: String,
    val status_id: Int,
    val user_type: Int
)*/
