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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gigzz.android.R
import com.gigzz.android.databinding.ItemJobFeedsBinding
import com.gigzz.android.domain.res.JobDetails
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.utils.formatDateTime
import com.gigzz.android.utils.getCombinedDateWithStartAndEndDate
import com.gigzz.android.utils.goneIf
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.postCreatedDateTime
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.google.android.material.tabs.TabLayoutMediator

class FeedAdapter(
    val list: ArrayList<PostData>,
    val userId: Int,
    val onItemClick: (pos: Int, model: PostData, src: String) -> Unit,
) : ListAdapter<PostData, FeedAdapter.MyViewHolder>(DIFF_CALLBACK) {

    inner class MyViewHolder(val binding: ItemJobFeedsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PostData) {
            binding.apply {
                if (data.postType=="J"){
                    postViewPager.remove()
                    tabLayout.remove()
                    cardCompanyJob.root.show()
                    data.jobDetails?.let { setJobData(it) }
                }else{
                    postViewPager.show()
                    tabLayout.show()
                    cardCompanyJob.root.remove()
                    if (data.mediaItem?.isNotEmpty() == true) {
                        postViewPager.show()
                        val viewPagerAdapter =
                            FeedViewPagerAdapter(
                                data.mediaItem!!
                            ) { src, model, viewPager ->
                                onItemClick(absoluteAdapterPosition, data, src)
                            }
                        postViewPager.adapter = viewPagerAdapter
                        if (data.mediaItem?.size!! > 1) {
                            tabLayout.show()
                            TabLayoutMediator(tabLayout, postViewPager) { tab, position ->
                            }.attach()
                        } else tabLayout.remove()
                    } else {
                        tabLayout.remove()
                        postViewPager.remove()
                    }
                }
                when (data.userType) {
                    1 -> {
                        tvUserType.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.gigzz_icon,
                            0,
                            0,
                            0
                        )
                        tvUserType.text = itemView.context.getString(R.string.job_seeker)
                       // tvUserType.compoundDrawables[0]?.setTint(itemView.context.getColor(R.color.pink_shade_1))
                    }

                    2 -> {
                        tvUserType.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.get_help_icon,
                            0,
                            0,
                            0
                        )
                        tvUserType.text = itemView.context.getString(R.string.get_help)
                        tvUserType.compoundDrawables[0]?.setTint(itemView.context.getColor(R.color.fab_color))
                    }

                    3 -> {
                        tvUserType.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.company_icon,
                            0,
                            0,
                            0
                        )
                        tvUserType.text = itemView.context.getString(R.string.company)
                        tvUserType.compoundDrawables[0]?.setTint(itemView.context.getColor(R.color.theme_blue))
                    }
                }

                data.cacheImage = ivUserPic.loadCachedImg(
                    data.userProfileImageUrl,
                    data.cacheImage,
                    R.drawable.user_placeholder
                )
                tvUserName.text = data.postedByUser
                tvDes.text = showSpannableString(
                    data.caption!!,
                    itemView.context,
                    absoluteAdapterPosition,
                    data
                )
                tvDes.movementMethod = LinkMovementMethod.getInstance()

                if (userId == data.userId) {
                    btnFollow.remove()
                } else {
                    when (data.isConnecting) {
                        0 -> {
                            btnFollow.show()
                            btnFollow.text = itemView.context.getString(R.string.action_connect)
                        }

                        1 -> {
                            btnFollow.show()
                            btnFollow.text = itemView.context.getString(R.string.requested)
                        }

                        2 -> btnFollow.remove()
                    }
                }

                tvLikeCount.text = data.totalLikesCount.toString()
                tvCommentCount.text = data.totalCommentsCount.toString()
                tvDateTime.text = data.createdDatetime?.let { postCreatedDateTime(it) }

                if (data.isLiked == 0) ivLike.setImageResource(R.drawable.unlike)
                else ivLike.setImageResource(R.drawable.like)

                ivLike.setOnClickListener {
                    handlePostLike(data)
                }

                btnFollow.setOnClickListener {
                    if (data.isConnecting == 0) {
                        btnFollow.text = itemView.context.getString(R.string.requested)
                    } else {
                        btnFollow.text = itemView.context.getString(R.string.action_connect)
                    }
                    onItemClick.invoke(bindingAdapterPosition, data, "connect")
                }

                ivUserPic.setOnClickListener {
                    onItemClick(bindingAdapterPosition, data, "profile")
                }

                tvUserName.setOnClickListener {
                    onItemClick(bindingAdapterPosition, data, "profile")
                }

                ivComment.setOnClickListener {
                    onItemClick(bindingAdapterPosition, data, "comment")
                }

                root.setOnClickListener {
                    onItemClick(
                        bindingAdapterPosition,
                        data,
                        "PostDetails")
                }

                itemView.setOnClickListener {
                    onItemClick(
                        bindingAdapterPosition,
                        data,
                        "PostDetails")
                }

                ivThreeDots.setOnClickListener {
                    onItemClick.invoke(bindingAdapterPosition, data, "ivThreeDots")
                }
            }
        }

        private fun setJobData(data: JobDetails){
            binding.cardCompanyJob.apply {
                jobName.text = data.jobTitle
                company.text= data.companyName
                //tvPostedDate.text = data.startDate
                jobDuration.goneIf(data.startDate.isNullOrEmpty())
                jobSalary.goneIf(data.compensation.isNullOrEmpty())
                jobSalary.text=data.compensation
                if (!jobTime.isVisible && !jobDuration.isVisible) {
                    jobDes.show()
                    jobDes.text = data.individualJobsDescription
                } else jobDes.remove()
                data.totalHours.let {
                    jobTime.text = itemView.context.getString(
                        R.string.total_hours,
                        data.totalHours.toString()
                    )
                }

                jobPlace.text = data.address

                if (!data.startDate.isNullOrEmpty() && !data.endDate.isNullOrEmpty()) {
                    val startDate =
                        formatDateTime(data.startDate, "yyyy-MM-dd", "dd-yyyy-MMM")
                    val endDate =
                        formatDateTime(data.endDate, "yyyy-MM-dd", "dd-yyyy-MMM")
                    val completeDate =
                        getCombinedDateWithStartAndEndDate(startDate, endDate)
                    jobDuration.text = completeDate
                }

                wishlist.hide()
            }
        }

        private fun handlePostLike(data: PostData) {
            if (data.isLiked == 1) {
                if (data.totalLikesCount!!.toInt() >= 1) {
                    onItemClick.invoke(absoluteAdapterPosition, data, "remove_like")
                }
            } else {
                onItemClick.invoke(absoluteAdapterPosition, data, "like")
            }
        }
    }


    fun showSpannableString(
        caption: String,
        context: Context,
        pos: Int,
        data: PostData,
    ): SpannableString {
        val spannableString = SpannableString(caption)

        val words = caption.split("\\s+".toRegex()) // Split text into words
        var startIndex = 0
        for (word in words) {
            if (word.startsWith("@") || word.startsWith("#")) {

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (word.startsWith("@")) {
                            onItemClick.invoke(pos, data, "tag")
                            //  handleAtWordClick(word,bindingAdapterPosition)
                        } else {
                            //  handleRegularWordClick(word)
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = ContextCompat.getColor(context, R.color.theme_blue)
                    }
                }
                // Apply blue color to words starting with "@" or "#"
                spannableString.setSpan(
                    clickableSpan,
                    /*   ForegroundColorSpan(Color.BLUE),*/
                    caption.indexOf(word),
                    caption.indexOf(word) + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                startIndex += word.length + 1
            }
        }
        return spannableString
    }


    override fun submitList(list: MutableList<PostData>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemJobFeedsBinding.inflate(
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
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PostData>() {
            override fun areItemsTheSame(
                oldItem: PostData,
                newItem: PostData,
            ): Boolean {
                return oldItem.postId == newItem.postId
            }

            override fun areContentsTheSame(
                oldItem: PostData,
                newItem: PostData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}

