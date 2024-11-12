package com.gigzz.android.ui.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemBlockedUsersBinding
import com.gigzz.android.domain.res.BlockedUSerData
import com.gigzz.android.domain.res.EducationDetailsData
import com.gigzz.android.utils.loadCachedImg

class BlockedUserAdapter(
    val list: List<BlockedUSerData>, private val onItemClick: (data: BlockedUSerData, pos: Int) -> Unit
) : RecyclerView.Adapter<BlockedUserAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ItemBlockedUsersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: BlockedUSerData) {
            data.cacheImage = binding.userProfileIv.loadCachedImg(
                data.userProfileImageUrl,
                data.cacheImage,
                R.drawable.user_placeholder
            )
            binding.blockedUserName.text = data.userName

            binding.unblockUserBtn.setOnClickListener {
                val name = list[absoluteAdapterPosition]
                val userId = list[absoluteAdapterPosition]
                onItemClick.invoke(data, absoluteAdapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemBlockedUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(list[position])
    }

    interface OnItemClickListener {
        fun onItemClick(name: String, userId: String)
    }

}