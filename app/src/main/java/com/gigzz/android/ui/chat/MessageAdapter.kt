package com.gigzz.android.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.databinding.ItemRecieveMessageBinding
import com.gigzz.android.databinding.ItemSentMessageBinding
import com.gigzz.android.domain.res.GetChatMessage
import com.gigzz.android.utils.postCreatedDateTime

private const val TYPE_MESSAGE_SENT = 0
private const val TYPE_MESSAGE_RECEIVED = 1

class MessageAdapter(private var selfId: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SentMessageViewHolder(var binding: ItemSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sentMsg: GetChatMessage) {
            binding.sentMsgTv.text = sentMsg.message
            binding.createdTime.text = postCreatedDateTime(sentMsg.createdAt)
        }
    }

    inner class ReceiveMessageViewHolder(val binding: ItemRecieveMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(receiveMsg: GetChatMessage) {
            binding.recieveMsgTv.text = receiveMsg.message
            binding.createdTime.text = postCreatedDateTime(receiveMsg.createdAt)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (msgDiffer.currentList[position].senderId.toInt() == selfId)
            TYPE_MESSAGE_SENT
        else TYPE_MESSAGE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_MESSAGE_RECEIVED -> {
                val binding =
                    ItemRecieveMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return ReceiveMessageViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemSentMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return SentMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TYPE_MESSAGE_SENT -> {
                (holder as SentMessageViewHolder).bind(msgDiffer.currentList[position])
            }

            TYPE_MESSAGE_RECEIVED -> {
                (holder as ReceiveMessageViewHolder).bind(msgDiffer.currentList[position])
            }
        }
    }

    override fun getItemCount() = msgDiffer.currentList.size

    private val differCallback = object : DiffUtil.ItemCallback<GetChatMessage>() {
        override fun areItemsTheSame(
            oldItem: GetChatMessage,
            newItem: GetChatMessage,
        ): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(
            oldItem: GetChatMessage,
            newItem: GetChatMessage,
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val msgDiffer = AsyncListDiffer(this, differCallback)

    fun saveData( dataResponse: List<GetChatMessage>){
        msgDiffer.submitList(dataResponse)
    }
}