package com.gigzz.android.domain.req

data class PostNewJobByIndividualReq(
    val category_name: String,
    val compensation: String,
    val description: String,
    val end_date: String,
    val end_time: String,
    val individual_job_url: String,
    val is_parent_supervision_required: Int,
    val job_name: String,
    val start_date: String,
    val start_time: String,
    val total_hours: String,
    val job_location: Int,
    val job_id: Int? =null
)