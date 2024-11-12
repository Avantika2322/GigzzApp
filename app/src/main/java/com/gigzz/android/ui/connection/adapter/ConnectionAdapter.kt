package com.gigzz.android.ui.connection.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.databinding.ItemUserConnectionsBinding
import com.gigzz.android.domain.res.ConnectionData
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.show

class ConnectionAdapter(
    private var from:String,
    private val onItemClick: (pos: Int, model: ConnectionData,src:String) -> Unit
) : ListAdapter<ConnectionData, ConnectionAdapter.MyViewHolder>(AllNourishmentDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemUserConnectionsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(getItem(position))
    }

    inner class MyViewHolder(private val binding: ItemUserConnectionsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: ConnectionData) {
            if (from == "connection") {
                binding.ivRequestImg.show()
                binding.tvAccept.hide()
            }else{
                binding.ivRequestImg.hide()
                binding.tvAccept.show()
            }
            binding.tvUserName.text= data.user_name

            binding.ivRequestImg.setOnClickListener {
                onItemClick.invoke(absoluteAdapterPosition, data,"request")
            }

            binding.tvAccept.setOnClickListener {
                onItemClick.invoke(absoluteAdapterPosition,data,"accept")
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
