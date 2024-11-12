package com.gigzz.android.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentAccountSettingBinding
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.onboarding.MainActivity
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.isNetworkAvailable
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.loadImage
import com.gigzz.android.utils.loadImageFromS3
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import kotlinx.coroutines.launch


class AccountSettingFragment : Fragment(R.layout.fragment_account_setting) {
    private val binding by viewBinding(FragmentAccountSettingBinding::bind)
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        clickListeners()
    }

    private fun initViews() = binding.apply {
        toolbar.toolbarTitle.text = getString(R.string.profile)
        toolbar.iv2.hide()
        lifecycleScope.launch {
            val data = profileViewModel.getUserDetail()
            ivUserPic.loadImageFromS3(
                data.profileImageUrl,
                R.drawable.user_placeholder
            )
            binding.tvUserName.text = data.fullName
            binding.tvAddress.text = data.address
        }
    }

    private fun clickListeners() = binding.run {
        toolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewBlock.setOnClickListener {
            findNavController().navigate(R.id.action_accountSettingFragment_to_blockedUsersFragment)
        }

        viewDelete.setOnClickListener {
            showAlertDialog(
                getString(R.string.delete_account),
                getString(R.string.delete_account_message)
            )
        }

        viewLogout.setOnClickListener {
            showAlertDialog(
                getString(R.string.logout_title),
                getString(R.string.logout_message)
            )
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (title == getString(R.string.logout_title)) {
                    profileViewModel.deleteTokenApi()
                } else {
                    profileViewModel.deleteAccountApi()
                }
                handleDeleteLogoutRes()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDeleteLogoutRes() {
        profileViewModel.commonRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.progressBar.hide()

                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(),getString(R.string.no_internet))
                }
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Success -> {
                    binding.progressBar.hide()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    activity?.finish()
                }
            }
        }
    }
}