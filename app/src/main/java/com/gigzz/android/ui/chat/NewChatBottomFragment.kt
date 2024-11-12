package com.gigzz.android.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.OnNewChatClickListener
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentNewChatBottomBinding
import com.gigzz.android.domain.res.ConnectionData
import com.gigzz.android.presentation.ChatViewModel
import com.gigzz.android.presentation.CircleViewModel
import com.gigzz.android.ui.connection.adapter.ConnectionAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewChatBottomFragment : BottomSheetDialogFragment(R.layout.fragment_new_chat_bottom) {
    private val binding by viewBinding(FragmentNewChatBottomBinding::bind)
    var onChatClick: OnNewChatClickListener? = null
    private val viewmodel by viewModels<CircleViewModel>()
    private val chatViewmodel by activityViewModels<ChatViewModel>()
    private lateinit var connectionAdapter: ConnectionAdapter
    private var myConnectionList = ArrayList<ConnectionData>()
    private var pageNo=1
    private var name=""
    private var image:String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // onChatClick?.onChatClicked(pos)
        viewmodel.getMyConnections(pageNo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        handleCreateChatRes()
    }

    private fun initView() = binding.apply{
        connectionAdapter= ConnectionAdapter("newChat") { pos, model, src->
            when(src){
                "accept"->{
                    chatViewmodel.createChatReq.chatType=1
                    chatViewmodel.createChatReq.secondUserId=model.user_id
                    name=model.user_name
                    image=model.user_profile_image_url
                    chatViewmodel.createChatApi()
                }
            }
        }
        rvConnections.adapter=connectionAdapter

        handleAllConnectionRes()
    }

    private fun handleAllConnectionRes() {
        viewmodel.getAllConnectionRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                rvConnections.hide()
                            }else{
                                rvConnections.show()
                                if (pageNo == 1) {
                                    myConnectionList.clear()
                                }
                                myConnectionList.addAll(data)
                                connectionAdapter.submitList(myConnectionList)
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

    private fun handleCreateChatRes() {
        chatViewmodel.createChatRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        findNavController().navigate(
                            R.id.action_chatFragment_to_messageFragment,
                            bundleOf(
                                "chatModel" to data,
                                "secondUserName" to name,
                                "secondUserImage" to image,
                            )
                        )
                        dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        chatViewmodel.clearCreateChatRes()
    }
}