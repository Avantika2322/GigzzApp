package com.gigzz.android.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentOtherUserProfileBinding
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

class OtherUserProfileFragment : Fragment(R.layout.fragment_other_user_profile) {
    private val binding by viewBinding(FragmentOtherUserProfileBinding::bind)
    private val profileViewModel by activityViewModels<ProfileViewModel>()

    companion object{
        var userId = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId= arguments?.getInt("userId")!!
        profileViewModel.getOtherUserProfileApi(userId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListeners()
        initTabLayout()
        handleMyProfileRes()
    }

    private fun initView() = binding.apply{
        toolbar.toolbarTitle.text= getString(R.string.profile)
        toolbar.wishlist.hide()
    }

    private fun initTabLayout() = binding.apply {
        userInfoPager.adapter =
            ProfileViewPagerAdapter(this@OtherUserProfileFragment)

        TabLayoutMediator(
            tabProfile, binding.userInfoPager
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.text = "About"
                1 -> tab.text = "Posts"
                2 -> tab.text = "Jobs"
                3 -> tab.text = "Reviews"
            }
        }.attach()
    }

    private fun clickListeners() = binding.run{
        toolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        toolbar.iv2.setOnClickListener {
            //findNavController().navigate(R.id.action_profileFragment_to_accountSettingFragment)
        }
    }

    private fun handleMyProfileRes() {
        profileViewModel.getMyProfileRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.show()
                    binding.groupProfile.hide()
                }

                is Resource.Success -> {
                    binding.progressBar.remove()
                    binding.groupProfile.show()
                    it.data?.data?.let { data ->
                        binding.apply {
                            data.cachedProfileImageUrl = ivUserPic.loadCachedImg(
                                data.profileImageUrl,
                                data.cachedProfileImageUrl,
                                R.drawable.user_placeholder
                            )
                            ProfileFragment.userId =data.userId
                            tvAddress.text= data.address
                            when (data.userType) {
                                1 -> tvUserName.text = data.fullName
                                2 -> tvUserName.text= data.fullName
                                3 -> tvUserName.text = data.companyName
                            }

                            if (data.overAllRating == "") tvRatingCount.text = "0"
                            else tvRatingCount.text = data.overAllRating.toString()

                            tvConnectionCount.text = data.totalConnection.toString()
                        }
                    }
                }

                is Resource.Error -> {
                    binding.groupProfile.hide()
                    binding.progressBar.remove()
                }

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    binding.groupProfile.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {
                    binding.groupProfile.hide()
                    binding.progressBar.remove()
                }
            }
        }
    }

    inner class ProfileViewPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> OtherUserAboutFragment()
                1 -> OtherUserProflePostsFragment()
                2 -> ProfileJobsFragment()
                3 -> OtherUserProfileReviewsFragment()
                else -> OtherUserAboutFragment()
            }
        }

        override fun getItemCount(): Int {
            return 4
        }
    }
}