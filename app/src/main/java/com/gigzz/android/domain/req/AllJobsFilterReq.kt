package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class AllJobsFilterReq(
    @SerializedName("job_type")
    var jobType: Int? = -1,
    @SerializedName("max_radius")
    var maxRadius: Int? = 60,
    @SerializedName("min_radius")
    var minRadius: Int? = 0,
    @SerializedName("page_no")
    var pageNo: Int? = 1,
    @SerializedName("posted_time")
    var postedTime: Int? = -1
)