package com.gigzz.android.ui.profile.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemEducationBinding
import com.gigzz.android.databinding.ItemSkillsBinding
import com.gigzz.android.domain.res.EducationDetailsData
import com.gigzz.android.utils.getYearOnly

class EducationAdapter(
    private var values: List<EducationDetailsData>,
    private val onItemClick: (data: String, pos: Int) -> Unit,
) : RecyclerView.Adapter<EducationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemEducationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: EducationDetailsData) {
            binding.tvGrade.text = model.grade
            binding.tvCollageName.text = model.school
            val isPursuing =
                if (getYearOnly(model.endYear!!) == "0000") itemView.context.getString(R.string.currently_pursuing)
                else getYearOnly(model.endYear!!)
            binding.tvYear.text = isPursuing
            binding.tvDuration.text = itemView.context.getString(
                R.string.college_duration,
                getYearOnly(model.startYear!!),
                isPursuing
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEducationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(values[position])
    }
}