package com.gigzz.android.ui.profile.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemAddEducationLayoutBinding
import com.gigzz.android.databinding.ItemAllEducationListBinding
import com.gigzz.android.databinding.ItemEducationBinding
import com.gigzz.android.databinding.ItemSkillsBinding
import com.gigzz.android.domain.res.EducationDetailsData
import com.gigzz.android.utils.getYearOnly

class AllEducationAdapter(
    private var values: List<EducationDetailsData>,
    private val onItemClick: (data: EducationDetailsData?, pos: Int, src: String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var TYPE_ADD = 0
    private var TYPE_PLAN = 1

    inner class ViewHolder(val binding: ItemAllEducationListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: EducationDetailsData) {
            binding.tvCollageName.text = model.school
            binding.ivCollageImg.setImageResource(if (absoluteAdapterPosition % 2 == 0) R.drawable.edu_img_1 else R.drawable.edu_img_2)

            binding.ivEdit.setOnClickListener {
                onItemClick.invoke(model, absoluteAdapterPosition, "edit")
            }
        }
    }

    inner class AddViewHolder(var binding: ItemAddEducationLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            itemView.setOnClickListener {
                onItemClick.invoke(null, absoluteAdapterPosition, "new")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == values.size) TYPE_ADD else TYPE_PLAN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_PLAN -> {
                val binding = ItemAllEducationListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemAddEducationLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return AddViewHolder(binding)
            }
        }


    }

    override fun getItemCount() = values.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_PLAN -> {
                (holder as ViewHolder).bind(values[position])
            }

            TYPE_ADD -> {
                (holder as AddViewHolder).bind()
            }
        }
        //holder.bind(values[position])
    }
}