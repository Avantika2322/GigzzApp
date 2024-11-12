package com.gigzz.android.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemAllChatsBinding
import com.gigzz.android.domain.res.ChatListData
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.postCreatedDateTime

/*class ChatAdapter(
    private val onThreadClicked: (ChatListData) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(
        private val binding: ItemAllChatsBinding,
        onThreadClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onThreadClicked(bindingAdapterPosition) }
        }

        fun bind(thread: ChatListData) {
            binding.tvUserName.text = thread.otherUser
            binding.tvMsgTime.text = thread.lastMessageCreatedAt?.let { postCreatedDateTime(it) }
            if (thread.lastMessage?.isEmpty() == true)  binding.tvLastMsg.text =itemView.context.getString(R.string.say_hi)
            else binding.tvLastMsg.text = thread.lastMessage
            thread.cachedImage = binding.ivUserPic.loadCachedImg(
                thread.userProfileImageUrl,
                thread.cachedImage,
                R.drawable.user_placeholder
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAllChatsBinding.inflate(inflater, parent, false)
        return ChatViewHolder(binding) {
            onThreadClicked(differ.currentList[it])
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    private val differCallback = object : DiffUtil.ItemCallback<ChatListData>() {
        override fun areItemsTheSame(
            oldItem: ChatListData,
            newItem: ChatListData
        ): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(
            oldItem: ChatListData,
            newItem: ChatListData
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}*/

class ChatsListAdapter(
    private val onThreadClicked: (ChatListData) -> Unit
) : ListAdapter<ChatListData, ChatsListAdapter.MyViewHolder>(ChatItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAllChatsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(getItem(position))
    }

    inner class MyViewHolder(private val binding: ItemAllChatsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: ChatListData) {
            binding.tvUserName.text = data.otherUser
            binding.tvMsgTime.text = data.lastMessageCreatedAt?.let { postCreatedDateTime(it) }
            if (data.lastMessage?.isEmpty() == true)  binding.tvLastMsg.text =itemView.context.getString(R.string.say_hi)
            else binding.tvLastMsg.text = data.lastMessage
            data.cachedImage = binding.ivUserPic.loadCachedImg(
                data.userProfileImageUrl,
                data.cachedImage,
                R.drawable.user_placeholder
            )

            itemView.setOnClickListener {
                onThreadClicked.invoke(data)
            }

        }
    }

    object ChatItemDiffCallback : DiffUtil.ItemCallback<ChatListData>() {
        override fun areContentsTheSame(oldItem: ChatListData, newItem: ChatListData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: ChatListData, newItem: ChatListData): Boolean {
            return oldItem.chatId == newItem.chatId
        }
    }

    override fun submitList(list: List<ChatListData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}