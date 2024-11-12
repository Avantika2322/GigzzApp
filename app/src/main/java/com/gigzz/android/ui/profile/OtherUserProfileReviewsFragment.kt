package com.gigzz.android.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentOtherUserProfileReviewsBinding
import com.gigzz.android.domain.res.ReviewsData
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.profile.adapter.ProfileReviewsAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtherUserProfileReviewsFragment : Fragment(R.layout.fragment_other_user_profile_reviews) {
    private val binding by viewBinding(FragmentOtherUserProfileReviewsBinding::bind)
    private val profileViewModel by viewModels<ProfileViewModel>()
    private lateinit var reviewsAdapter: ProfileReviewsAdapter
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private var pageNo = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getOtherUserReviewsApi(OtherUserProfileFragment.userId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        handleAllPostRes()
    }


    private fun setAdapter() {
        reviewsAdapter = ProfileReviewsAdapter { pos, data, src ->
            onItemClick(pos, data, src)
        }
        binding.rvReviews.adapter = reviewsAdapter
        (binding.rvReviews.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        // binding.rvFeed.layoutManager?.onRestoreInstanceState(recyclerViewState)!!
    }

    private fun onItemClick(pos: Int, data: ReviewsData, src: String) {

    }

    private fun handleAllPostRes() {
        profileViewModel.getUserReviewsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()) {
                                rvReviews.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_review)
                                noDataFound.tvTitle.text=getString(R.string.no_review)
                                noDataFound.tvSubTitle.text=getString(R.string.no_review_msg)
                            } else {
                                rvReviews.show()
                                noDataFound.root.hide()
                                profileViewModel.reviewList.clear()
                                profileViewModel.reviewList.addAll(data)
                                reviewsAdapter.submitList(profileViewModel.reviewList)
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