package com.gigzz.android.ui.profile.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemSkillsBinding

class SkillAdapter(
    private var values: List<String>, private var selectedValues: List<String>,
    val isEdited: Boolean, private val onItemClick: (data: String, pos: Int) -> Unit,
) : RecyclerView.Adapter<SkillAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSkillsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.skill.setOnClickListener {
                if (!isEdited) {
                    onItemClick(data, absoluteAdapterPosition)
                }
            }

            val isSelected = selectedValues.contains(data)
            if (isSelected) {
                binding.skill.setTextColor(itemView.context.getColor(R.color.light_blue_shade_1))
                binding.skill.backgroundTintList =
                    itemView.context.getColorStateList(R.color.theme_blue)
            } else {
                binding.skill.backgroundTintList =
                    itemView.context.getColorStateList(R.color.light_blue_shade_1)
                binding.skill.setTextColor(itemView.context.getColor(R.color.black))
            }
           // binding.nameChip.isChecked = isSelected


            binding.skill.text = data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSkillsBinding.inflate(
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