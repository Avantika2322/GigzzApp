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
import com.gigzz.android.domain.res.CompanyJobData
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.utils.formatDateTime
import com.gigzz.android.utils.getCombinedDateWithStartAndEndDate
import com.gigzz.android.utils.goneIf
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show


class CompanyJobsAdapter(
    val onItemClick: (pos: Int, model: CompanyJobData, src: String) -> Unit
) : ListAdapter<CompanyJobData, CompanyJobsAdapter.MyViewHolder>(JobsDiffCallback) {

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

        fun setData(data: CompanyJobData) {
            with(binding) {
                jobName.text = data.company_name

                jobSalary.goneIf(data.salary.isNullOrEmpty())

                jobSalary.text = data.salary
                if (!jobTime.isVisible && !jobDuration.isVisible) {
                    jobDes.show()
                    jobDes.text = data.description
                } else jobDes.remove()


                jobAddress.text = data.address


                jobProgress.apply {
                    when (data.status_id) {
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

    object JobsDiffCallback : DiffUtil.ItemCallback<CompanyJobData>() {
        override fun areContentsTheSame(oldItem: CompanyJobData, newItem: CompanyJobData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: CompanyJobData, newItem: CompanyJobData): Boolean {
            return oldItem.job_type == newItem.job_type
        }
    }

    override fun submitList(list: List<CompanyJobData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}
