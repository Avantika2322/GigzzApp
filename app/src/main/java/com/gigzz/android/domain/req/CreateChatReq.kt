package com.gigzz.android.domain.req

import com.google.gson.annotations.SerializedName


class CreateChatReq(
    @SerializedName("second_user_id")
    var secondUserId: Int = 0,
    @SerializedName("job_id")
    var jobId: Int? = null,
    @SerializedName("chat_type")
    var chatType: Int = -1
)