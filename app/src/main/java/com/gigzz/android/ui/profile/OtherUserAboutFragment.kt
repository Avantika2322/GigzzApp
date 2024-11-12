package com.gigzz.android.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentOtherUserAboutBinding
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.showToast


class OtherUserAboutFragment : Fragment(R.layout.fragment_other_user_about) {
    private val binding by viewBinding(FragmentOtherUserAboutBinding::bind)
    private val profileViewModel by activityViewModels<ProfileViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleMyProfileRes()
    }

    private fun handleMyProfileRes() {
        profileViewModel.getMyProfileRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    it.data?.data?.let { it ->
                        binding.apply {
                            tvOverview.text = it.bio
                            tvLocation.text = it.address
                            //etLocation.text = it?.address?.substringAfterLast(",")
                            tvContact.text = it.phoneNumber
                            //userEmail.text = it?.emailId
                        }
                    }
                }

                is Resource.Error -> {}

                is Resource.InternetError -> {
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {}
            }
        }
    }
}