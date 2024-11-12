package com.gigzz.android.ui.jobs

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemSkillsBinding
import com.gigzz.android.domain.res.FilterData

class GigzzTypeAdapter(
    private val arrayList: List<FilterData>, private var selectedGigzz: String?,
    val onItemClick: (pos: Int, model: FilterData, src: String) -> Unit,
) : RecyclerView.Adapter<GigzzTypeAdapter.MyViewHolder>() {
    var selectedPosition = -1
    var lastItemSelectedPos = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSkillsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(arrayList[position], position == selectedPosition)

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class MyViewHolder(private val binding: ItemSkillsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FilterData, isSelected: Boolean) {
            data.isSelected= isSelected
            binding.skill.text = data.categoryName
            if (data.isSelected) {
                //selectedPosition= position
                binding.skill.setBackgroundResource(R.drawable.bg_corner_radius5dp)
                binding.skill.backgroundTintList= ColorStateList.valueOf(itemView.context.getColor(R.color.theme_blue))
                binding.skill.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                /*binding.skill.setBackgroundResource(R.drawable.bg_corner_radius5dp)
                binding.skill.backgroundTintList=ColorStateList.valueOf(itemView.context.getColor(R.color.grey_shade_1))
                binding.skill.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))*/
                if (!selectedGigzz.isNullOrEmpty() && selectedGigzz == data.categoryName) {
                    binding.skill.setBackgroundResource(R.drawable.bg_corner_radius5dp)
                    binding.skill.backgroundTintList=ColorStateList.valueOf(itemView.context.getColor(R.color.theme_blue))
                    binding.skill.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }else{
                    binding.skill.setBackgroundResource(R.drawable.bg_corner_radius5dp)
                    binding.skill.backgroundTintList=ColorStateList.valueOf(itemView.context.getColor(R.color.grey_shade_1))
                    binding.skill.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
            }



            itemView.setOnClickListener {
                selectedPosition = absoluteAdapterPosition
                if(lastItemSelectedPos == -1)
                    lastItemSelectedPos = selectedPosition
                else {
                    notifyItemChanged(lastItemSelectedPos)
                    lastItemSelectedPos = selectedPosition
                }
                notifyItemChanged(selectedPosition)
                onItemClick(absoluteAdapterPosition, data, "root")
            }
        }
    }
}