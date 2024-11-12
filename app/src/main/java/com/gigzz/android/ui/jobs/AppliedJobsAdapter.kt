package com.gigzz.android.ui.jobs


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemJobFeedsBinding
import com.gigzz.android.databinding.ItemJobsGigzzBinding
import com.gigzz.android.domain.res.JobData
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.utils.formatDateTime
import com.gigzz.android.utils.getCombinedDateWithStartAndEndDate
import com.gigzz.android.utils.goneIf
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.postCreatedDateTime
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.google.android.material.tabs.TabLayoutMediator

class AppliedJobsAdapter(
    val onItemClick: (pos: Int, model: JobsData, src: String) -> Unit,
) : ListAdapter<JobsData, AppliedJobsAdapter.MyViewHolder>(DIFF_CALLBACK) {

    inner class MyViewHolder(val binding: ItemJobsGigzzBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: JobsData) {
            with(binding)  {
                company.text = data.companyName
                tvPostedDate.text= data.createdDatetime
                data.cachedUserProfileImageUrl = ivUserPic.loadCachedImg(
                    data.userProfileImageUrl,
                    data.cachedUserProfileImageUrl,
                    R.drawable.user_placeholder
                )

                wishlist.hide()
                jobProgress.show()

                jobDuration.goneIf(data.startDate.isNullOrEmpty())

                if (!data.totalHours.isNullOrEmpty()) jobTime.text =
                    itemView.context.getString(
                        R.string.total_hours,
                        data.totalHours.toString()
                    )
                else if (data.totalHours.isNullOrEmpty() && data.jobTypes == null) {
                    jobTime.remove()
                } else {
                    jobTime.show()
                    when (data.jobType) {
                        1 -> jobTime.text = itemView.context.resources.getString(R.string.full_time)
                        2 -> jobTime.text = itemView.context.resources.getString(R.string.part_time)
                        3 -> jobTime.text = itemView.context.resources.getString(R.string.both)
                    }
                }

                if (!jobTime.isVisible && !jobDuration.isVisible) {
                    jobDes.show()
                    jobDes.text = data.individualJobsDescription
                } else jobDes.remove()

                if (data.jobType == 2) {
                    if (data.companyAddress.isNullOrEmpty()) jobPlace.remove()
                    else {
                        jobPlace.show()
                        jobPlace.text = data.companyAddress
                    }
                } else jobPlace.text = data.address

                if (!data.jobName.isNullOrEmpty()) jobName.text = data.jobName
                else jobName.text = data.jobTitle

                if (!data.startDate.isNullOrEmpty() && !data.endDate.isNullOrEmpty()) {
                    val startDate = formatDateTime(data.startDate, "yyyy-MM-dd", "dd-yyyy-MMM")
                    val endDate = formatDateTime(data.endDate, "yyyy-MM-dd", "dd-yyyy-MMM")
                    val completeDate = getCombinedDateWithStartAndEndDate(startDate, endDate)
                    jobDuration.text = completeDate
                } else {
                    if (data.experience.isNullOrEmpty()) jobDuration.remove()
                    else {
                        if (data.experience != "No") {
                            jobDuration.show()
                            if (data.experience.isDigitsOnly() && data.experience.toDouble() < 1) jobDuration.text = itemView.context.getString(R.string.no_experience_required)
                            else jobDuration.text = itemView.context.getString(
                                R.string.total_experience,
                                data.experience.toString()
                            )
                        } else {
                            // jobDuration.gone()
                            jobDuration.text =
                                itemView.context.getString(R.string.no_experience)
                        }

                    }
                }

                jobProgress.apply {
                    when {
                        (data.statusId == 1 || data.jobStatus == "1") -> {
                            setTextColor(itemView.context.getColor(R.color.yellow_shade_1))
                            backgroundTintList =
                                ColorStateList.valueOf(itemView.context.getColor(R.color.transparent_10_yellow))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.in_procress_icon, 0, 0, 0)
                            text = itemView.context.getString(R.string.in_process)
                        }

                        data.statusId == 2 || data.jobStatus == "2" -> {
                            setTextColor(itemView.context.getColor(R.color.green))
                            backgroundTintList =
                                ColorStateList.valueOf(itemView.context.getColor(R.color.transparent_10_green))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_right_icon, 0, 0, 0)
                            text = if (data.userType == 1) itemView.context.getString(R.string.hired)
                            else itemView.context.getString(R.string.completed)
                        }

                        data.statusId == 3 || data.jobStatus == "3" -> {
                            if (data.userType == 1) {
                                setTextColor(itemView.context.getColor(`in`.aabhasjindal.otptextview.R.color.red))
                                backgroundTintList =
                                    ColorStateList.valueOf(itemView.context.getColor(R.color.transparent_10_red))
                                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_group_wrong, 0, 0, 0)
                                text = itemView.context.getString(R.string.rejected)
                            } else {
                                setTextColor(itemView.context.getColor(R.color.yellow_shade_1))
                                backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#FFF7E5"))
                                setCompoundDrawablesWithIntrinsicBounds(R.drawable.in_procress_icon, 0, 0, 0)
                                text = itemView.context.getString(R.string.in_process)
                            }
                        }

                        else -> hide()
                    }
                }

                if (data.salary != null) {
                    jobSalary.visibility = View.VISIBLE
                    jobSalary.text = itemView.context.getString(
                        R.string.total_salary,
                        data.salary.toString()
                    )
                } else jobSalary.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick.invoke(absoluteAdapterPosition, data, "root")
            }

        }
    }


    override fun submitList(list: MutableList<JobsData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemJobsGigzzBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<JobsData>() {
            override fun areItemsTheSame(
                oldItem: JobsData,
                newItem: JobsData,
            ): Boolean {
                return oldItem.jobId == newItem.jobId
            }

            override fun areContentsTheSame(
                oldItem: JobsData,
                newItem: JobsData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}

