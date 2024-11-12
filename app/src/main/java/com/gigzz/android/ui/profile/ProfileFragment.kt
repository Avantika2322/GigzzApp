package com.gigzz.android.ui.profile

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentProfileBinding
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.home.FeedAdapter
import com.gigzz.android.ui.jobs.AllJobsFragment
import com.gigzz.android.ui.jobs.AppliedJobsFragment
import com.gigzz.android.ui.jobs.MyJobsFragment
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val binding by viewBinding(FragmentProfileBinding::bind)
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)
    private var isFirstCall = false
    private lateinit var popupWindow: PopupWindow
    private var imageName: String? = null
    private var filePath: File? = null
    private var month: String? = null
    private var year: Int? = null

    companion object {
        var userId: Int? = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstCall = true
        profileViewModel.getMyProfileApi()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initTabLayout()
        clickListeners()
        handleMyProfileRes()
    }

    private fun initView() = binding.apply {
        toolbar.toolbarTitle.text = getString(R.string.profile)
        toolbar.wishlist.hide()
    }


    private fun initTabLayout() = binding.apply {
        userInfoPager.adapter =
            ProfileViewPagerAdapter(this@ProfileFragment)

        TabLayoutMediator(
            tabProfile, binding.userInfoPager
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.text = "About"
                1 -> tab.text = "Posts"
                2 -> tab.text = "Reviews"
            }
        }.attach()
    }

    private fun clickListeners() = binding.run {
        toolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        toolbar.iv2.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_accountSettingFragment)
        }

        tvEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        ivEditImg.setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage() {
        ImagePicker.Companion.with(this.requireActivity())
            .crop()
            .cropOval()
            .maxResultSize(512, 512, true)
            .provider(ImageProvider.BOTH) //Or bothCameraGallery()
            .createIntentFromDialog { launcher.launch(it) }

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                binding.ivUserPic.setImageURI(uri)
                filePath = uri.toFile()
                val formatMonth = SimpleDateFormat("MMMM")
                val curMonth: String = formatMonth.format(Date(System.currentTimeMillis()))
                month = curMonth.substring(0, 3)
                val cal = Calendar.getInstance()
                year = cal[Calendar.YEAR]
                imageName =
                    "profile_image/${year}/${month}/UserProfileImage_${System.currentTimeMillis()}.jpeg"

                profileViewModel.uploadToS3(filePath!!, imageName!!)
                uploadMediaObserver()
            }
        }

    private fun handleMyProfileRes() {
        profileViewModel.getMyProfileRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.show()
                    if (isFirstCall) binding.groupProfile.hide()
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
                            userId = data.userId
                            tvAddress.text = data.address
                            when (data.userType) {
                                1 -> tvUserName.text = data.fullName
                                2 -> tvUserName.text = data.fullName
                                3 -> tvUserName.text = data.companyName
                            }

                            profileViewModel.model.apply {
                                address = data.address
                                phoneNumber = data.phoneNumber
                                zipCode = data.zipCode
                                bio = data.bio
                                lat = data.lat
                                lon = data.lon
                                profileImage = data.profileImageUrl
                            }

                            if (data.overAllRating == "") tvRatingCount.text = "0"
                            else tvRatingCount.text = data.overAllRating.toString()

                            tvConnectionCount.text = data.totalConnection.toString()
                        }
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.remove()
                    binding.groupProfile.hide()
                }

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    binding.groupProfile.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {
                    binding.progressBar.remove()
                    binding.groupProfile.hide()
                }
            }
        }
    }

    private fun uploadMediaObserver() {
        profileViewModel.uploadProfilePic.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Success -> {
                    res.data.let {
                        profileViewModel.model.profileImage = it
                        profileViewModel.editProfileApi(profileViewModel.model)
                        handleEditProfileResponse()
                    }

                    binding.progressBar.hide()
                }

                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), res.message.toString())
                }

                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }
            }
        }
    }

    private fun handleEditProfileResponse() {
        profileViewModel.editUserProfileRes.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success->{
                    binding.progressBar.remove()
                }

                is Resource.Error -> binding.progressBar.remove()
                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(),getString(R.string.no_internet))
                }
                is Resource.Loading ->  binding.progressBar.show()
            }
        }
    }

    inner class ProfileViewPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AboutProfileFragment()
                1 -> ProfilePostsFragment()
                2 -> ProfileReviewsFragment()
                else -> AboutProfileFragment()
            }
        }

        override fun getItemCount(): Int {
            return 3
        }
    }
}