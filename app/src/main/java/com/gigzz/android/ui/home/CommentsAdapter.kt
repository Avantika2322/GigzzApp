package com.gigzz.android.ui.home


import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.CommentsItemBinding
import com.gigzz.android.databinding.ItemJobFeedsBinding
import com.gigzz.android.domain.res.CommentData
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.loadImage
import com.gigzz.android.utils.postCreatedDateTime
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.google.android.material.tabs.TabLayoutMediator

class CommentsAdapter(
    val userId: Int,
    val onItemClick: (pos: Int, model: CommentData) -> Unit
) : ListAdapter<CommentData, CommentsAdapter.MyViewHolder>(DIFF_CALLBACK) {

    inner class MyViewHolder(val binding: CommentsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CommentData) {
            binding.apply {
                profilePic.loadImage(
                    S3Utils.generateS3ShareUrl(
                        itemView.context,
                        data.userProfileImageUrl
                    ), R.drawable.user_placeholder
                )
                commentsText.text = data.comment
                notificationTime.text =
                    postCreatedDateTime(data.createdDatetime)///2023-11-29T05:53:21.000Z
                options.setOnClickListener { onItemClick(bindingAdapterPosition, data) }
            }
        }
    }

    override fun submitList(list: MutableList<CommentData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CommentsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommentData>() {
            override fun areItemsTheSame(
                oldItem: CommentData,
                newItem: CommentData,
            ): Boolean {
                return oldItem.commentId == newItem.commentId
            }

            override fun areContentsTheSame(
                oldItem: CommentData,
                newItem: CommentData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}

