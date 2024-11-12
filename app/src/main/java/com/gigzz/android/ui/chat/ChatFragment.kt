package com.gigzz.android.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentChatBinding
import com.gigzz.android.databinding.FragmentCreatePasswordBinding
import com.gigzz.android.domain.res.ChatListData
import com.gigzz.android.domain.res.ConnectionData
import com.gigzz.android.presentation.ChatViewModel
import com.gigzz.android.presentation.CircleViewModel
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.ui.connection.adapter.ConnectionRequestAdapter
import com.gigzz.android.ui.jobs.JobsFragment
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadImageFromS3
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val binding by viewBinding(FragmentChatBinding::bind)
    private val viewmodel by activityViewModels<ChatViewModel>()
    private val feedViewModel: HomeViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatsListAdapter
    private var myConnectionList = ArrayList<ChatListData>()
    private var pageNo=1
    var imageUrl: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewmodel.getAllChatApi(pageNo)
        lifecycleScope.launch {
            val profileRes = feedViewModel.getProfileRes()
            imageUrl = profileRes.profileImageUrl
        }
        initView()
        handleAllChatRes()
        setClickListener()
    }

    private fun initView() = binding.apply {
        chatAdapter=ChatsListAdapter {
            findNavController().navigate(R.id.action_chatFragment_to_messageFragment,
                bundleOf("thread" to it)
            )
        }
        rvAllChat.adapter=chatAdapter
        (rvAllChat.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        topView.ivSearch.hide()

        chipApplicants.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked) chipApplicants.setChipIconTintResource(R.color.white)
            else chipApplicants.setChipIconTintResource(R.color.pink_shade_1)
        }

        topView.userDp.loadImageFromS3(
            imageUrl,
            R.drawable.user_placeholder
        )
    }

    private fun setClickListener() = binding.run {
        chipGroupChat.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                R.id.chipAll -> {
                    viewmodel.getAllChatApi(1)
                    //handleAllChatRes()
                }

                R.id.chipApplicants -> {
                    viewmodel.getAllApplicantChatApi(pageNo)
                    handleApplicantsChatResponse()
                }
            }
        }

        tvNewChat.setOnClickListener {
            val shareOption = NewChatBottomFragment()
            /*shareOption.onChatClick=this
            position=pos*/
            shareOption.show(childFragmentManager, "bottom")
        }

        topView.userDp.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_my_profile_nav_graph)
        }
    }

    private fun handleApplicantsChatResponse(){
        viewmodel.getAllApplicantChatRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.threads?.let { data ->
                        data.forEach { it.isFrom = "Applicants" }
                        val filteredList = data.filter { it.isBlocked != 1 }
                        binding.apply {
                            if (data.isEmpty()){
                                rvAllChat.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.ic_no_chat_messages)
                                noDataFound.tvTitle.text=getString(R.string.no_messages)
                                noDataFound.tvSubTitle.text=getString(R.string.no_msg_body)
                            }else{
                                rvAllChat.show()
                                noDataFound.root.hide()
                                if (pageNo == 1) {
                                    myConnectionList.clear()
                                }
                                myConnectionList.addAll(data)
                                chatAdapter.submitList(myConnectionList)
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

    private fun handleAllChatRes() {
        viewmodel.getAllChatRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.threads?.let { data ->
                        data.forEach { it.isFrom = "Connection" }
                        val filteredList = data.filter { it.isBlocked != 1 }
                        binding.apply {
                            if (data.isEmpty()){
                                rvAllChat.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.ic_no_chat_messages)
                                noDataFound.tvTitle.text=getString(R.string.no_messages)
                                noDataFound.tvSubTitle.text=getString(R.string.no_msg_body)
                            }else{
                                rvAllChat.show()
                                noDataFound.root.hide()
                                if (pageNo == 1) {
                                    myConnectionList.clear()
                                }
                                myConnectionList.addAll(data)
                                chatAdapter.submitList(myConnectionList)
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