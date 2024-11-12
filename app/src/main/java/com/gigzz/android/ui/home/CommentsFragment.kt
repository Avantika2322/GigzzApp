package com.gigzz.android.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentCommentsBinding
import com.gigzz.android.databinding.FragmentHomeBinding
import com.gigzz.android.domain.res.CommentData
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.isNetworkAvailable
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showKeyBoard
import com.gigzz.android.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommentsFragment : Fragment(R.layout.fragment_comments) {
    private val binding by viewBinding(FragmentCommentsBinding::bind)
    private val feedViewModel: HomeViewModel by activityViewModels()
    private var page = 1
    private lateinit var commentsAdapter: CommentsAdapter
    private var commentList = ArrayList<CommentData>()
    var postId = -1
    var commentId = -1
    var position = -1
    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentList.clear()
        postId = arguments?.getInt("postId")!!
        feedViewModel.getAllComments(1, postId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentsAdapter = CommentsAdapter(feedViewModel.userId) { position, data ->
            openAlertDialog(data,position)
        }
        binding.rvComment.adapter = commentsAdapter
        (binding.rvComment.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        handleAllCommentRes()
        initViews()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        toolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        postBtn.setOnClickListener {
            val msg = editComment.text.toString().trim()
            if (type=="edit") {
                feedViewModel.editCommentOnPostApi(
                    postId = postId,
                    commentId = commentId,
                    comment = msg
                )
            }
            else {
                if (msg.isNotEmpty()) {
                    feedViewModel.addCommentOnPostApi(
                        postId = postId,
                        comment = editComment.text.toString().trim()
                    )
                    feedViewModel.postList.forEachIndexed { index, postData ->
                        if (postData.postId == postId) {
                            postData.totalCommentsCount = postData.totalCommentsCount?.plus(1)
                            feedViewModel.postList[index] = postData
                        }
                    }
                }
            }
            handlePostCommentsResponse()
            binding.editComment.setText("")
        }
    }

    private fun initViews() = binding.apply {
        toolbar.toolbarTitle.text = getString(R.string.comments)
        toolbar.iv2.hide()
    }

    private fun openAlertDialog(model: CommentData,pos: Int) {
        position=pos
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
                binding.editComment.setText(model.comment)
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
            when (res) {
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Error -> binding.progressBar.hide()
                is Resource.InternetError -> {
                    showToast(requireContext(), getString(R.string.no_internet))
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
                            commentList.removeAt(position)
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
                            if (data.isEmpty()) {
                                rvComment.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_post_found)
                                noDataFound.tvTitle.text = getString(R.string.no_post_found)
                                noDataFound.tvSubTitle.text = getString(R.string.no_post_text)
                            } else {
                                rvComment.show()
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