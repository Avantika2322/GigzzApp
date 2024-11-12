package com.gigzz.android.utils

import androidx.annotation.StringRes
import com.gigzz.android.R

enum class Circle {
    MATCHING,
    REQUESTS
}

enum class RequestAction { ACCEPT, DECLINE }
enum class JobDate(@StringRes val title: Int) {
    START(R.string.job_start_day_msg),
    END(R.string.job_end_day_msg);
}

enum class JobTime { START, END }

enum class EducationTime { START, END }
enum class VolumeState {
    ON, OFF
}