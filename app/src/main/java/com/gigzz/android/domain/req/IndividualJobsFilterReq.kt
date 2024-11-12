package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class IndividualJobsFilterReq(
    @SerializedName("category_name")
    var categoryName1: String? = null,
    @SerializedName("job_status")
    var jobStatus: Int? = null,
    @SerializedName("max_compensation")
    var maxCompensation: Int? = null,
    @SerializedName("min_compensation")
    var minCompensation: Int? = null,
    @SerializedName("page_no")
    val pageNo: Int? = null
)