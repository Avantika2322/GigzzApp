package com.gigzz.android.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentBlockedUsersBinding
import com.gigzz.android.domain.res.BlockedUSerData
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.profile.adapter.BlockedUserAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast


class BlockedUsersFragment : Fragment(R.layout.fragment_blocked_users) {
    private val binding by viewBinding(FragmentBlockedUsersBinding::bind)
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)
    private var blockedUserData: ArrayList<BlockedUSerData> = ArrayList()
    private lateinit var blockedUserAdapter: BlockedUserAdapter
    var page: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getAllBlockedUsersApi(page)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blockedUserAdapter = BlockedUserAdapter(blockedUserData){data, pos ->
            data.userName?.let { showUnblockConfirmationDialog(it, data.userId.toString()) }
        }
        binding.rvBlockedUsers.adapter = blockedUserAdapter
        handleBlockedUsersRes()
        setClickListener()
    }

    private fun handleBlockedUsersRes() {
        profileViewModel.getAllBlockedUsersRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()) {
                                rvBlockedUsers.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_block_user)
                                noDataFound.tvTitle.text=getString(R.string.no_block_user)
                                noDataFound.tvSubTitle.text=getString(R.string.no_block_user_msg)
                            } else {
                                rvBlockedUsers.show()
                                noDataFound.root.hide()
                                blockedUserData.clear()
                                blockedUserData.addAll(data)
                                blockedUserAdapter.notifyDataSetChanged()
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


    private fun setClickListener() = binding.run {
        toolbar.btnBack.setOnClickListener { findNavController().popBackStack() }
    }


    private fun showUnblockConfirmationDialog(name: String, userId: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirm Unblock")
            .setMessage("Are you sure you want to unblock $name?")
            .setPositiveButton("Yes") { _, _ ->
                //profileViewModel.unblockUsers(userId)
                handleUnblockUser(userId)
            }
            .setNegativeButton("No", null)
            .setCancelable(true)
            .create()
        alertDialog.show()
    }

    private fun handleUnblockUser(userId: String) {
        /*viewModel.unblockUserResponse.observe(viewLifecycleOwner) { response ->
            when (response.statusCode) {
                StatusCode.OK -> {
                    toast(response.message!!)
                    blockedUserData.removeAll { it.userId.toString() == userId }
                    adapter.notifyDataSetChanged()
                    checkNoBlockUser()
                }

                else -> response.message?.let { toast(it) }
            }
        }*/
    }

}