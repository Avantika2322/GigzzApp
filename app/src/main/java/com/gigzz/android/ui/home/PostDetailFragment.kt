package com.gigzz.android.ui.home

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentPostDetailBinding
import com.gigzz.android.domain.res.CommentData
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.isNetworkAvailable
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.postCreatedDateTime
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {
    private val binding by viewBinding(FragmentPostDetailBinding::bind)
    private val feedViewModel: HomeViewModel by activityViewModels()
    var userId: Int? = 0
    var pos = -1
    private var page=1
    private lateinit var commentsAdapter: CommentsAdapter
    private var commentList = ArrayList<CommentData>()
    var commentId = -1
    var commentPosition = -1
    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pos = arguments?.getInt("pos")!!
        feedViewModel.postList[pos].postId?.let { feedViewModel.getAllComments(pageNumber = page, it) }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val profileRes = feedViewModel.getProfileRes()
            userId = profileRes.userId
        }

        initView(pos)
        handleAllCommentRes()
        clickListener()
    }

    private fun initView(pos: Int?) {
        binding.apply {
            feedViewModel.postList[pos!!].let { data ->
                when (data.userType) {
                    1 -> {
                        tvUserType.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.gigzz_icon,
                            0,
                            0,
                            0
                        )
                        tvUserType.text = getString(R.string.job_seeker)
                        // tvUserType.compoundDrawables[0]?.setTint(itemView.context.getColor(R.color.pink_shade_1))
                    }

                    2 -> {
                        tvUserType.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.get_help_icon,
                            0,
                            0,
                            0
                        )
                        tvUserType.text = getString(R.string.get_help)
                        tvUserType.compoundDrawables[0]?.setTint(
                            getColor(
                                requireContext(),
                                R.color.fab_color
                            )
                        )
                    }

                    3 -> {
                        tvUserType.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.company_icon,
                            0,
                            0,
                            0
                        )
                        tvUserType.text = getString(R.string.company)
                        tvUserType.compoundDrawables[0]?.setTint(
                            getColor(
                                requireContext(),
                                R.color.theme_blue
                            )
                        )
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
                    requireContext()
                )
                tvDes.movementMethod = LinkMovementMethod.getInstance()
                if (userId == data.userId) {
                    btnFollow.remove()
                } else {
                    when (data.isConnecting) {
                        0 -> {
                            btnFollow.show()
                            btnFollow.text = getString(R.string.action_connect)
                        }

                        1 -> {
                            btnFollow.show()
                            btnFollow.text = getString(R.string.requested)
                        }

                        2 -> btnFollow.remove()
                    }
                }
                tvLikeCount.text = data.totalLikesCount.toString()
                tvCommentCount.text = data.totalCommentsCount.toString()
                tvDateTime.text = data.createdDatetime?.let { postCreatedDateTime(it) }

                if (data.isLiked == 0) ivLike.setImageResource(R.drawable.unlike)
                else ivLike.setImageResource(R.drawable.like)

                if (data.mediaItem?.isNotEmpty() == true) {
                    postViewPager.show()
                    val viewPagerAdapter =
                        FeedViewPagerAdapter(
                            data.mediaItem!!
                        ) { src, model, viewPager ->
                            //onItemClick(absoluteAdapterPosition, data, src)
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

        }

        commentsAdapter = CommentsAdapter(feedViewModel.userId){position,data->
            commentPosition=position
            openAlertDialog(data)
        }
        binding.commentsRv.adapter = commentsAdapter
        (binding.commentsRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun clickListener() = binding.run {
        ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        postBtn.setOnClickListener {
            val msg= writeCommentTv.text.toString().trim()
            if (type=="edit") {
                feedViewModel.editCommentOnPostApi(
                    postId = feedViewModel.postList[pos].postId!!,
                    commentId = commentId,
                    comment = msg
                )
            }
            else {
                if (msg.isNotEmpty()) {
                    feedViewModel.addCommentOnPostApi(
                        postId = feedViewModel.postList[pos].postId!!,
                        comment = msg
                    )
                    feedViewModel.postList[pos].totalCommentsCount = feedViewModel.postList[pos].totalCommentsCount?.plus(1)
                    tvCommentCount.text = feedViewModel.postList[pos].totalCommentsCount.toString()
                }
            }
            handlePostCommentsResponse()
            binding.writeCommentTv.setText("")
        }
    }

    private fun showSpannableString(
        caption: String,
        context: Context
    ): SpannableString {
        val spannableString = SpannableString(caption)

        val words = caption.split("\\s+".toRegex()) // Split text into words
        var startIndex = 0
        for (word in words) {
            if (word.startsWith("@") || word.startsWith("#")) {

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (word.startsWith("@")) {
                            //onItemClick.invoke(pos, data, "tag")
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

    private fun openAlertDialog(model: CommentData) {
        val alertDialog = BottomSheetDialog(requireContext())
        alertDialog.setContentView(R.layout.options_comment)
        if (feedViewModel.userId != model.commentBy) {
            val btnReport = alertDialog.findViewById<TextView>(R.id.edit)!!
            alertDialog.findViewById<TextView>(R.id.delete_comment)!!.remove()
            alertDialog.findViewById<View>(R.id.share_view)!!.remove()
            btnReport.text = getString(R.string.report_comment)
            val newDrawableResId = R.drawable.report
            val newDrawable =
                ContextCompat.getDrawable(requireContext(), newDrawableResId)?.mutate()
            DrawableCompat.setTint(
                newDrawable!!,
                ContextCompat.getColor(requireContext(), R.color.theme_blue)
            )
            btnReport.setCompoundDrawablesWithIntrinsicBounds(newDrawable, null, null, null)
            btnReport.setOnClickListener {
                /*findNavController().navigate(
                    CommentsFragmentDirections.actionCommentsFragmentToReportUserFragment(
                        id = model.commentId.toString(),
                        root = "Comment",
                        postId = args.postId
                    )
                )*/
                alertDialog.dismiss()
            }
        } else if (feedViewModel.userId == model.commentBy) {
            commentId = model.commentId
            alertDialog.findViewById<TextView>(R.id.edit)?.setOnClickListener {
                type = "edit"
                binding.writeCommentTv.setText(model.comment)
                alertDialog.dismiss()
            }

            alertDialog.findViewById<TextView>(R.id.delete_comment)?.setOnClickListener {
                type= "deleteComment"
                showMessageBox(model.commentId)
                alertDialog.dismiss()
            }

        }
        alertDialog.show()

    }

    private fun showMessageBox(commentId: Int?) {
        val alertDialog: AlertDialog = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.MyRounded_MaterialComponents_MaterialAlertDialog
        )
            .setView(R.layout.common_alert_dialog)
            .show()
        alertDialog.findViewById<Button>(R.id.btnYes)?.setOnClickListener {
            if (commentId != null) {
                feedViewModel.deleteCommentOnPostApi(commentId)
                handlePostCommentsResponse()
            }
            alertDialog.dismiss()
        }
        alertDialog.findViewById<Button>(R.id.btnNo)?.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun handlePostCommentsResponse() {
        feedViewModel.commonResponse.observe(viewLifecycleOwner) { res ->
            when(res){
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Error ->binding.progressBar.hide()
                is Resource.InternetError -> {
                    showToast(requireContext(),getString(R.string.no_internet))
                    binding.progressBar.hide()
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    when(type){
                        "edit"->{
                            type=""
                            feedViewModel.clearCreatePostResponse()
                        }
                        "deleteComment"->{
                            type=""
                            commentList.removeAt(commentPosition)
                            commentsAdapter.submitList(commentList)
                            feedViewModel.clearCreatePostResponse()
                        }
                        else->feedViewModel.clearCreatePostResponse()
                    }
                }
            }
        }
    }

    private fun handleAllCommentRes() {
        feedViewModel.getAllCommentRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                commentsRv.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_post_found)
                                noDataFound.tvTitle.text=getString(R.string.no_post_found)
                                noDataFound.tvSubTitle.text=getString(R.string.no_post_text)
                            }else{
                                commentsRv.show()
                                noDataFound.root.hide()
                                if (page == 1) {
                                    commentList.clear()
                                }
                                commentList.addAll(data)
                                commentsAdapter.submitList(commentList)
                            }
                        }
                    }
                }

                is Resource.Error -> binding.progressBar.remove()

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> binding.progressBar.remove()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        feedViewModel.clearCommentResponse()
    }
}