package com.gigzz.android.ui.profile.adapter


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
import com.gigzz.android.databinding.ItemProfilePostsBinding
import com.gigzz.android.databinding.ItemReviewsBinding
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.domain.res.ReviewsData
import com.gigzz.android.ui.home.FeedViewPagerAdapter
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.postCreatedDateTime
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.google.android.material.tabs.TabLayoutMediator

class ProfileReviewsAdapter(
    val onItemClick: (pos: Int, model: ReviewsData, src: String) -> Unit
) : ListAdapter<ReviewsData, ProfileReviewsAdapter.MyViewHolder>(DIFF_CALLBACK) {

    inner class MyViewHolder(val binding: ItemReviewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ReviewsData) {
            binding.apply {
            }
        }
    }

    override fun submitList(list: MutableList<ReviewsData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemReviewsBinding.inflate(
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
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ReviewsData>() {
            override fun areItemsTheSame(
                oldItem: ReviewsData,
                newItem: ReviewsData,
            ): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(
                oldItem: ReviewsData,
                newItem: ReviewsData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}

