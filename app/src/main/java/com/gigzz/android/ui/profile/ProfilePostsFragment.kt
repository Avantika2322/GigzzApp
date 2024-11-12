package com.gigzz.android.ui.profile

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentProfilePostsBinding
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.home.FeedAdapter
import com.gigzz.android.ui.profile.adapter.ProfilePostsAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class ProfilePostsFragment : Fragment(R.layout.fragment_profile_posts) {
    private val binding by viewBinding(FragmentProfilePostsBinding::bind)
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private lateinit var postsAdapter: ProfilePostsAdapter
    private var pageNo=1
    private lateinit var popupWindow: PopupWindow
    var userId: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getMyPostsApi(pageNo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            userId = homeViewModel.getProfileRes().userId
        }
        setAdapter()
        handleAllPostRes()
    }

    private fun setAdapter() {
        postsAdapter = ProfilePostsAdapter { pos, data, src ->
            onItemClick(pos,data, src)
        }
        binding.rvPosts.adapter = postsAdapter
        (binding.rvPosts.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun onItemClick(pos: Int, model: PostData, src: String) {
        when (src) {
            "PostDetails" -> {}

            "ivThreeDots" -> {
                showPostFilter(
                    binding.rvPosts.findViewHolderForAdapterPosition(pos)?.itemView!!, model
                )
            }

            "like" -> {
                homeViewModel.postList[pos].let {
                    homeViewModel.postList[pos] =
                        it.copy(
                            isLiked = 1,
                            totalLikesCount = (it.totalLikesCount?.plus(1))
                        )
                }
                homeViewModel.likePostApi(model.postId!!)
                postsAdapter.submitList(homeViewModel.postList)
            }

            "remove_like" -> {
                homeViewModel.postList[pos].let {
                    homeViewModel.postList[pos] =
                        it.copy(
                            isLiked = 0,
                            totalLikesCount = if (it.totalLikesCount!! > 0) it.totalLikesCount?.minus(
                                1
                            ) else 0
                        )
                }
                homeViewModel.removeLikeApi(model.postId!!)
                postsAdapter.submitList(homeViewModel.postList)
            }

            "comment" -> {
               /* findNavController().navigate(
                    R.id.action_homeFragment_to_commentsFragment,
                    bundleOf("postId" to model.postId)
                )*/
            }
        }
    }

    private fun showPostFilter(anchorView: View, model: PostData) {
        val customDropdownView = layoutInflater.inflate(R.layout.post_options_dropdown, null)
        popupWindow = PopupWindow(
            customDropdownView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        if (model.userId == userId) {
            customDropdownView.findViewById<View>(R.id.report).remove()
            customDropdownView.findViewById<View>(R.id.share_view).remove()

        }
        customDropdownView.findViewById<View>(R.id.share).setOnClickListener {
            val textToShare =
                "I just see this Gigzz Post " + model.caption + " ! " + "https://www.gigzz.com"
            sharePost(textToShare)
            popupWindow.dismiss()
        }

        customDropdownView.findViewById<View>(R.id.report).setOnClickListener {
            /* findNavController().navigate(
                 FeedFragmentDirections.actionFeedFragmentToReportUserFragment(
                     model.postId.toString(), "Feed", model.postId.toString()
                 )
             )*/
            popupWindow.dismiss()
        }

        val location = IntArray(2)
        anchorView.getLocationInWindow(location)
        val x = location[0] + anchorView.width
        val y = location[1] + 80

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)

        popupWindow.setOnDismissListener {
            popupWindow.dismiss()
        }
    }

    private fun sharePost(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun handleAllPostRes() {
        profileViewModel.getUserPostsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                rvPosts.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_post_found)
                                noDataFound.tvTitle.text=getString(R.string.no_post_found)
                                noDataFound.tvSubTitle.text=getString(R.string.no_post_text)
                            }else{
                                rvPosts.show()
                                noDataFound.root.hide()
                                if (pageNo == 1) {
                                    profileViewModel.postList.clear()
                                }
                                profileViewModel.postList.addAll(data)
                                postsAdapter.submitList(profileViewModel.postList)
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
}