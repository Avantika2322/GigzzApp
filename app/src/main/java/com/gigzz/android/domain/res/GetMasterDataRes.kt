package com.gigzz.android.domain.res

import com.google.gson.annotations.SerializedName

data class GetMasterDataRes(
    val data: MasterData,
    val message: String
)

data class MasterData(
    @SerializedName("flag_resion")
    val flagReason: ArrayList<FlagReason>?,
    @SerializedName("industry_type")
    val industryType: ArrayList<IndustryType>?,
    val passion: ArrayList<Passion>?,
    val skills: ArrayList<Skill>?,
    val category: ArrayList<Category>?,

    )
data class Category(
    val category_id: Int,
    val name: String,
    val category_job_type:Int
)

data class FlagReason(
    val flag_id: Int,
    val flagged_reason: String
)
data class IndustryType(
    val name: String
)
data class Passion(
    val passion: String
)
data class Skill(
    val skill_name: String
)