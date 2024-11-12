package com.gigzz.android.ui.jobs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemCompanyGigzzBinding
import com.gigzz.android.databinding.ItemJobsGigzzBinding
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.utils.formatDateTime
import com.gigzz.android.utils.getCombinedDateWithStartAndEndDate
import com.gigzz.android.utils.goneIf
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show


class IndividualJobsAdapter(
    val onItemClick: (pos: Int, model: JobsData, src: String) -> Unit
) : ListAdapter<JobsData, IndividualJobsAdapter.MyViewHolder>(JobsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemCompanyGigzzBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(getItem(position))
    }

    inner class MyViewHolder(private val binding: ItemCompanyGigzzBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: JobsData) {
            with(binding) {
                jobName.text = data.jobName
                tvPostedDate.text = data.createdDatetime
                jobDuration.goneIf(data.startDate.isNullOrEmpty())
                jobSalary.goneIf(data.compensation.isNullOrEmpty())

                jobSalary.text = data.compensation
                if (!jobTime.isVisible && !jobDuration.isVisible) {
                    jobDes.show()
                    jobDes.text = data.individualJobsDescription
                } else jobDes.remove()
                data.totalHours.let {
                    jobTime.text = itemView.context.getString(
                        R.string.total_hours,
                        data.totalHours.toString()
                    )
                }

                jobAddress.text = data.address

                if (!data.startDate.isNullOrEmpty()) {
                    val startDate =
                        formatDateTime(data.startDate!!, "yyyy-MM-dd", "dd-yyyy-MMM")
                    val endDate =
                        formatDateTime(data.endDate!!, "yyyy-MM-dd", "dd-yyyy-MMM")
                    val completeDate =
                        getCombinedDateWithStartAndEndDate(startDate, endDate)
                    jobDuration.text = completeDate
                }

                jobProgress.apply {
                    when (data.statusId) {
                        null, 1 -> {
                            setTextColor(itemView.context.getColor(R.color.green))
                            backgroundTintList =
                                ColorStateList.valueOf(itemView.context.getColor(R.color.transparent_10_green))
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.in_procress_icon,
                                0,
                                0,
                                0
                            )
                            text = itemView.context.getString(R.string.open)
                        }

                        2 -> {
                            setTextColor(itemView.context.getColor(R.color.green))
                            backgroundTintList =
                                ColorStateList.valueOf(itemView.context.getColor(R.color.transparent_10_green))
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_right_icon,
                                0,
                                0,
                                0
                            )
                            text = itemView.context.getString(R.string.completed)
                        }

                        else -> {
                            setTextColor(itemView.context.getColor(R.color.yellow_shade_1))
                            backgroundTintList =
                                ColorStateList.valueOf(itemView.context.getColor(R.color.transparent_10_yellow))
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.in_procress_icon,
                                0,
                                0,
                                0
                            )
                            text = itemView.context.getString(R.string.in_process)
                        }
                    }
                }
            }

            itemView.setOnClickListener {
                onItemClick.invoke(absoluteAdapterPosition, data, "root")
            }

        }
    }

    object JobsDiffCallback : DiffUtil.ItemCallback<JobsData>() {
        override fun areContentsTheSame(oldItem: JobsData, newItem: JobsData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: JobsData, newItem: JobsData): Boolean {
            return oldItem.jobId == newItem.jobId
        }
    }

    override fun submitList(list: List<JobsData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}
