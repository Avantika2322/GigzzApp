package com.gigzz.android.ui.connection.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemAllRequestsBinding
import com.gigzz.android.databinding.ItemUserConnectionsBinding
import com.gigzz.android.domain.res.ConnectionData
import com.gigzz.android.utils.loadCachedImg

class ConnectionRequestAdapter(
    private val onItemClick: (pos: Int, model: ConnectionData,src: String) -> Unit
) : ListAdapter<ConnectionData, ConnectionRequestAdapter.MyViewHolder>(AllNourishmentDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAllRequestsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(getItem(position))
    }

    inner class MyViewHolder(private val binding: ItemAllRequestsBinding) :
        RecyclerView.ViewHolder(binding.root) {
            val ctx=itemView.context
        fun setData(data: ConnectionData) {

            binding.tvUserName.text= data.user_name
            val startIcon= when (data.user_type){
                1-> R.drawable.gigzz_icon
                2-> R.drawable.get_help_icon
                else-> R.drawable.company_icon
            }
            binding.tvUserType.setCompoundDrawablesWithIntrinsicBounds(startIcon, 0, 0, 0)

            binding.tvUserType.text= when (data.user_type){
                1-> ctx.getString(R.string.gigzz)
                2-> ctx.getString(R.string.get_help)
                else-> ctx.getString(R.string.company)
            }
            data.cached_user_profile_image_url= binding.ivUserPic.loadCachedImg(data.user_profile_image_url,data.cached_user_profile_image_url,R.drawable.user_placeholder)

            binding.tvAccept.setOnClickListener {
                onItemClick.invoke(absoluteAdapterPosition, data,"accept")
            }

            binding.tvDelete.setOnClickListener {
                onItemClick.invoke(absoluteAdapterPosition, data,"delete")
            }
        }
    }

    object AllNourishmentDiffCallback : DiffUtil.ItemCallback<ConnectionData>() {
        override fun areContentsTheSame(oldItem: ConnectionData, newItem: ConnectionData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: ConnectionData, newItem: ConnectionData): Boolean {
            return oldItem.user_id == newItem.user_id
        }
    }

    override fun submitList(list: List<ConnectionData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}
