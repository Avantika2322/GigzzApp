package com.gigzz.android.domain.req

data class PostNewJobByCompany(
    val address: String,
    val age_requirement: Int,
    val category_id: Int?=0,
    val job_id: Int?,
    val category_name: String,
    val company_job_url: String,
    val company_name: String,
    val description: String,
    val experience: String,
    val images: List<Image>,
    val job_title: String,
    val job_types: ArrayList<Int>,
    val lat: Double,
    val lon: Double,
    val salary: String,
    val zip_code: String,
    val job_location: Int,
    val post_type: String
)

data class Image(
    val image_thumbnail: String,
    val image_url: String
)
