package com.gigzz.android.domain.res

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class EducationDetailRes(
    var data: ArrayList<EducationDetailsData>?,
    var message: String? = null,
)

@Parcelize
data class EducationDetailsData(
    @SerializedName("education_id") var educationId: Int? = -1,
    var school: String? = "",
    var grade: String? = "",
    @SerializedName("start_year") var startYear: String? = "",
    @SerializedName("end_year") var endYear: String? = "",
    @SerializedName("is_pursuing") var isPursuing: Int? = -1,
) : Parcelable
