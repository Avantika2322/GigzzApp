package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class CompanyJobsFilterReq(
    @SerializedName("age_requirement")
    var ageRequirement: Int? = null,
    @SerializedName("category_name")
    var categoryName: String? = null,
    @SerializedName("job_status")
    var jobStatus: Int? = null,
    @SerializedName("job_types")
    var jobTypes: ArrayList<Int>? = null,
    @SerializedName("max_compensation")
    var maxCompensation: Int? = null,
    @SerializedName("min_compensation")
    var minCompensation: Int? = null,
    @SerializedName("page_no")
    var pageNo: Int? = null
)