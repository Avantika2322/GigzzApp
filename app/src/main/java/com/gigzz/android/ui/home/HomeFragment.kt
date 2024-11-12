package com.gigzz.android.ui.home

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
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentCreatePasswordBinding
import com.gigzz.android.databinding.FragmentHomeBinding
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadImage
import com.gigzz.android.utils.loadImageFromS3
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val feedViewModel: HomeViewModel by activityViewModels()
    private lateinit var popupWindow: PopupWindow
    var page = 1
    private var feedAdapter: FeedAdapter? = null
    var imageUrl: String? = ""
    var userId: Int? = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val profileRes = feedViewModel.getProfileRes()
            userId = profileRes.userId
            imageUrl = profileRes.profileImageUrl
            feedViewModel.userType = profileRes.userType!!
        }
        feedViewModel.getAllPosts(pageNumber = 1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setClickListener()
        handleAllPostRes()
    }

    private fun initViews() = with(binding) {
        topView.userDp.loadImageFromS3(imageUrl, R.drawable.user_placeholder)

        chipCompany.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked) chipCompany.setChipIconTintResource(R.color.white)
            else chipCompany.setChipIconTintResource(R.color.theme_blue)
        }

        chipGigzz.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked) chipGigzz.setChipIconTintResource(R.color.white)
            else chipGigzz.setChipIconTintResource(R.color.pink_shade_1)
        }

        chipHelp.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked) chipHelp.setChipIconTintResource(R.color.white)
            else chipHelp.setChipIconTintResource(R.color.fab_color)
        }

        topView.ivSearch.hide()
        setAdapter()
    }

    private fun setAdapter() {
        feedAdapter = FeedAdapter(
            feedViewModel.postList, userId!!
        ) { pos, data, src ->
            onItemClick(data, src, pos)
        }
        binding.rvFeed.adapter = feedAdapter
        (binding.rvFeed.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        // binding.rvFeed.layoutManager?.onRestoreInstanceState(recyclerViewState)!!
    }

    private fun setClickListener() = binding.run {
        refreshLayout.setOnRefreshListener {
            feedViewModel.getAllPosts(pageNumber = 1)
            refreshLayout.isRefreshing = false
        }

        feedChipGroup.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                R.id.chipAll -> {
                    feedViewModel.userType = 0
                    handleUserTypeChange(feedViewModel.userType)
                }

                R.id.chipCompany -> {
                    feedViewModel.userType = 3
                    handleUserTypeChange(feedViewModel.userType)
                }

                R.id.chipGigzz -> {
                    feedViewModel.userType = 2
                    handleUserTypeChange(feedViewModel.userType)
                }

                R.id.chipHelp -> {
                    feedViewModel.userType = 1
                    handleUserTypeChange(feedViewModel.userType)
                }
            }
        }

        topView.userDp.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myProfileNavGraph)
        }
    }

    private fun onItemClick(model: PostData, src: String, pos: Int) {
        when (src) {
            "PostDetails" -> {
                findNavController().navigate(R.id.action_homeFragment_to_postDetailFragment,
                    bundleOf("pos" to pos)
                )
                feedAdapter?.submitList(feedViewModel.postList)
            }

            "ivThreeDots" -> {
                showPostFilter(
                    binding.rvFeed.findViewHolderForAdapterPosition(pos)?.itemView!!, model
                )
            }

            "like" -> {
                feedViewModel.postList[pos].let {
                    feedViewModel.postList[pos] =
                        it.copy(
                            isLiked = 1,
                            totalLikesCount = (it.totalLikesCount?.plus(1))
                        )
                }
                feedViewModel.likePostApi(model.postId!!)
                feedAdapter?.submitList(feedViewModel.postList)
            }

            "remove_like" -> {
                feedViewModel.postList[pos].let {
                    feedViewModel.postList[pos] =
                        it.copy(
                            isLiked = 0,
                            totalLikesCount = if (it.totalLikesCount!! > 0) it.totalLikesCount?.minus(
                                1
                            ) else 0
                        )
                }
                feedViewModel.removeLikeApi(model.postId!!)
                feedAdapter?.submitList(feedViewModel.postList)
            }

            "connect" -> {
                if (model.isConnecting == 0) {
                    model.isConnecting = 1
                    model.userId?.let {
                        feedViewModel.sendConnectionApi(it)
                    }
                    connectUser(model, pos)
                } else {
                    model.isConnecting = 0
                    model.userId?.let {
                        feedViewModel.cancelConnectionApi(it)
                    }
                    cancelRequest(model, pos)
                }
            }

            "profile" -> {
                if (userId == model.userId) {
                    findNavController().navigate(R.id.action_homeFragment_to_myProfileNavGraph)
                } else {
                    if (model.isConnecting == 2) findNavController().navigate(
                        R.id.action_homeFragment_to_otherUserProfileFragment,
                        bundleOf(
                            "userId" to model.userId, "postId" to model.postId
                        )
                    )
                    else showMessageBox(model, pos)
                }
            }

            "comment" -> {
                findNavController().navigate(
                    R.id.action_homeFragment_to_commentsFragment,
                    bundleOf("postId" to model.postId)
                )
                feedAdapter?.submitList(feedViewModel.postList)
            }

            "tag" -> {
                findNavController().navigate(
                    R.id.action_homeFragment_to_otherUserProfileFragment,
                    bundleOf(
                        "userId" to model.userId, "postId" to model.postId
                    )
                )
            }

            "Player" -> {
                findNavController().navigate(
                    R.id.action_homeFragment_to_videoPlayerFragment,
                    bundleOf("media" to model.mediaItem?.get(pos))
                )
            }
        }
    }

    private fun connectUser(model: PostData, pos: Int) {
        for ((index, i) in feedViewModel.postList.withIndex()) {
            if (i.userId == model.userId && index != pos) feedViewModel.postList[index] =
                i.copy(isConnecting = 1)
        }
        feedAdapter?.submitList(feedViewModel.postList)
        // feedAdapter.notifyDataSetChanged()
    }

    private fun cancelRequest(model: PostData, pos: Int) {
        for ((index, i) in feedViewModel.postList.withIndex()) {
            if (i.userId == model.userId && index != pos) feedViewModel.postList[index] =
                i.copy(isConnecting = 0)
        }
        feedAdapter?.submitList(feedViewModel.postList)
        // feedAdapter.notifyDataSetChanged()
    }

    private fun handleUserTypeChange(newUserType: Int) {
        feedViewModel.userType=newUserType
        feedViewModel.getAllPosts(pageNumber = 1)
    }

    private fun showMessageBox(model: PostData, pos: Int) {
        val alertDialog: AlertDialog = MaterialAlertDialogBuilder(
            requireActivity(), R.style.MyRounded_MaterialComponents_MaterialAlertDialog
        ).setView(R.layout.dialog_locked_profile).show()
        val btnConnect = alertDialog.findViewById<Button>(R.id.connect_btn)
        when (model.isConnecting) {
            0 -> {
                btnConnect?.text = getString(R.string.action_connect)
                btnConnect?.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.theme_blue
                    )
                )
                btnConnect?.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(), R.color.white
                    )
                )
            }

            1 -> {
                btnConnect?.text = getString(R.string.requested)
                btnConnect?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                btnConnect?.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(), R.color.theme_blue
                    )
                )
            }
        }
        btnConnect?.setOnClickListener {
            if (model.isConnecting == 0) {
                model.isConnecting = 1
                btnConnect.text = getString(R.string.requested)
                btnConnect.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                btnConnect.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(), R.color.theme_blue
                    )
                )
                model.userId?.let {
                    feedViewModel.sendConnectionApi(it)
                }
                connectUser(model, pos)

            } else {
                model.isConnecting = 0
                btnConnect.text = getString(R.string.action_connect)
                btnConnect.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.theme_blue
                    )
                )
                btnConnect.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(), R.color.white
                    )
                )
                model.userId?.let { feedViewModel.cancelConnectionApi(it) }
                cancelRequest(model, pos)

            }
        }
        alertDialog.show()
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
        feedViewModel.getAllPostRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                rvFeed.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_post_found)
                                noDataFound.tvTitle.text=getString(R.string.no_post_found)
                                noDataFound.tvSubTitle.text=getString(R.string.no_post_text)
                            }else{
                                rvFeed.show()
                                noDataFound.root.hide()
                                if (page == 1) {
                                    feedViewModel.postList.clear()
                                }
                                feedViewModel.postList.addAll(data)
                                feedAdapter?.submitList(feedViewModel.postList)
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