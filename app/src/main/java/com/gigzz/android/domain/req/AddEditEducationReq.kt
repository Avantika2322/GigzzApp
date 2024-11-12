package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName

data class AddEditEducationReq(
    var school: String? = null,
    var grade: String? = null,
    @SerializedName("start_year") var startYear: String? = null,
    @SerializedName("end_year") var endYear: String? = null,
    @SerializedName("is_pursuing") var isPursuing: Int? = null,
    @SerializedName("education_id") var educationId: Int? = null
)
