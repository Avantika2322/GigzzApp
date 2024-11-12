package com.gigzz.android.ui.chat

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.data.socket.SocketEventListener
import com.gigzz.android.data.socket.SocketHelper
import com.gigzz.android.databinding.FragmentMessageBinding
import com.gigzz.android.domain.res.ChatListData
import com.gigzz.android.domain.res.CreateData
import com.gigzz.android.domain.res.GetChatMessage
import com.gigzz.android.presentation.ChatViewModel
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.hideKeyboard
import com.gigzz.android.utils.isNetworkAvailable
import com.gigzz.android.utils.loadImage
import com.gigzz.android.utils.parcelable
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.gigzz.android.utils.toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessageFragment : Fragment(R.layout.fragment_message), SocketEventListener {
    private val binding by viewBinding(FragmentMessageBinding::bind)
    private lateinit var messageAdapter: MessageAdapter
    private var thread: ChatListData? = null
    private var model: CreateData? = null
    private var otherUserId: Int? = null
    private var selfId: Int? = null
    private var otherUserName = ""
    private var otherUserImage = ""
    private var pageNo = 1
    private lateinit var popupWindow: PopupWindow
    private var scrollingToBottom: Boolean = false
    private var isConnected: Int? = null
    private val feedViewModel: HomeViewModel by viewModels()
    private val viewModel by activityViewModels<ChatViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            selfId = viewModel.getUserId()
        }

        if (arguments != null) {
            thread = arguments?.parcelable("thread")
            model = arguments?.parcelable("chatModel")
            otherUserName = arguments?.getString("secondUserName").toString()
            otherUserImage = arguments?.getString("secondUserImage").toString()
            isConnected = if (thread != null) thread?.isConnecting else model?.isConnecting
        }
        //  }
        otherUserId =
            if (model != null) if (model?.firstUserId == selfId) model?.secondUserId else model?.firstUserId
            else if (thread?.firstUserId == selfId) thread?.secondUserId else thread?.firstUserId

        if (model != null) SocketHelper.chatId = model!!.chatId
        else SocketHelper.chatId = thread?.chatId

        SocketHelper.chatType =if (model != null) {
            if(model?.chatType!=null) model?.chatType!! else 1
        }
        else {
            if (thread?.isFrom == "Connection") 1 else 2
        }
        SocketHelper.myUserId = selfId
        SocketHelper.otherUserId = otherUserId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SocketHelper.init()
        SocketHelper.turnOnSocket()
        scrollOnClickKeyboard()
        SocketHelper.initEventListener(this)
        setClickListener()
        initView()
    }

    private fun initView() = binding.apply {
        val layoutManager2 = LinearLayoutManager(context)
        layoutManager2.stackFromEnd = true
        messageRv.layoutManager = layoutManager2
        messageAdapter = MessageAdapter(selfId!!)
        messageRv.apply {
            adapter = messageAdapter
            itemAnimator = DefaultItemAnimator()
        }

        if (model != null) {
            tvUserName.text = otherUserName
            ivUserPic.loadImage(
                S3Utils.generateS3ShareUrl(requireActivity(), otherUserImage),
                R.drawable.user_placeholder
            )
        } else {
            tvUserName.text = thread?.otherUser
            ivUserPic.loadImage(
                S3Utils.generateS3ShareUrl(requireActivity(), thread?.userProfileImageUrl),
                R.drawable.user_placeholder
            )
        }

        chatMessageEditText.addTextChangedListener(messageTextWatcher)

        if (thread?.chatId != null) {
            if (thread?.isFrom == "Connection" || model?.chatType == 1) {
                viewModel.getMessagesApi(pageNo,thread?.chatId!!)
                handleAllMessagesResponse()
            } else {
                viewModel.getApplicantMessagesApi(pageNo,thread?.chatId!!)
                handleApplicantMessagesResponse()
            }
        } else {
            viewModel.getMessagesApi(pageNo, model?.chatId!!)
            handleAllMessagesResponse()
        }

    }

    private val messageTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val message = binding.chatMessageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                binding.ivSend.isEnabled = true
                binding.ivSend.setImageResource(R.drawable.ic_send_msg)
            } else {
                binding.ivSend.isEnabled = false
                binding.ivSend.setImageResource(R.drawable.ic_send_msg_inactive)
            }

        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private fun setClickListener() = binding.run {
        ivMoreOption.setOnClickListener {
            hideKeyboard(chatMessageEditText)
            showMoreOptions(ivMoreOption)
        }
        ivSend.setOnClickListener {
            noDataFound.root.hide()
            messageRv.show()
            if (chatMessageEditText.text.isNotEmpty()) {
                if (thread?.isFrom == "Connection" || model?.chatType == 1) SocketHelper.chatType =
                    1 else SocketHelper.chatType = 2
                SocketHelper.sentMessageEvent(chatMessageEditText.text.toString().trim())
                chatMessageEditText.setText("")
            }
        }
        ivUserPic.setOnClickListener {
            if (isConnected == 0 || isConnected == 1)
                showMessageBox()
            else {
                /*findNavController().navigate(
                    ChatScreenFragmentDirections.actionChatScreenFragmentToOtherUserProfileFragment(
                        userId = otherUserId.toString()
                    )
                )*/
            }
        }
        ivBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun showMessageBox() {
        val alertDialog: androidx.appcompat.app.AlertDialog = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.MyRounded_MaterialComponents_MaterialAlertDialog
        )
            .setView(R.layout.dialog_locked_profile)
            .show()
        val btnConnect = alertDialog.findViewById<Button>(R.id.connect_btn)
        when (isConnected) {
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
                        requireActivity(),
                        R.color.white
                    )
                )
            }

            1 -> {
                btnConnect?.text = getString(R.string.requested)
                btnConnect?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                btnConnect?.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.theme_blue
                    )
                )
            }
        }
        btnConnect?.setOnClickListener {
            if (isConnected == 0) {
                isConnected = 1
                btnConnect.text = getString(R.string.requested)
                btnConnect.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                btnConnect.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.theme_blue
                    )
                )
                if (isNetworkAvailable(requireContext())) otherUserId?.let {
                    feedViewModel.sendConnectionApi(it)
                }
                else showToast(requireContext(), getString(R.string.no_internet))
                //handleSentConnectionRequestResponse()
            } else {
                isConnected = 0
                btnConnect.text = getString(R.string.action_connect)
                btnConnect.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.theme_blue
                    )
                )
                btnConnect.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                if (isNetworkAvailable(requireContext())) otherUserId?.let {
                    feedViewModel.cancelConnectionApi(it)
                }
                else showToast(requireContext(), getString(R.string.no_internet))
                // handleCancelConnectionRequestResponse()
            }
            /*alertDialog.dismiss()*/
        }
        alertDialog.show()
    }

    private fun scrollOnClickKeyboard() {
        binding.messageRv.viewTreeObserver.addOnGlobalLayoutListener {
            try {
                val r = Rect()
                binding.messageRv.getWindowVisibleDisplayFrame(r)
                val screenHeight: Int = binding.messageRv.rootView.height
                val keypadHeight = screenHeight - r.bottom
                if (keypadHeight > screenHeight * 0.15) {
                    if (!scrollingToBottom) {
                        scrollingToBottom = true
                        binding.messageRv.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                } else {
                    scrollingToBottom = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showMoreOptions(view: View) {
        val popUpChat = layoutInflater.inflate(R.layout.pop_up_chat_option, null)
        popupWindow = PopupWindow(
            popUpChat,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popUpChat.findViewById<View>(R.id.tvReportPost).setOnClickListener {
            showConfirmationDialog()
            popupWindow.dismiss()
        }
        popUpChat.findViewById<View>(R.id.tvBlockUser).setOnClickListener {
            showBlockConfirmationDialog()
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(view)
        popupWindow.setOnDismissListener {
            popupWindow.dismiss()
        }
    }

    private fun showConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Clear Chats")
            .setMessage("Are you sure you want to clear chats with ${thread?.otherUser}?")
            .setPositiveButton("Yes") { _, _ ->
                if (thread?.chatId != null) {
                    viewModel.deleteChatApi(thread?.chatId!!)
                }
                handleDeleteChatResponse()
            }
            .setNegativeButton("No", null)
            .setCancelable(true)
            .create()
        alertDialog.show()
    }

    private fun showBlockConfirmationDialog() {
        val alertDialog: androidx.appcompat.app.AlertDialog = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.MyRounded_MaterialComponents_MaterialAlertDialog
        )
            .setView(R.layout.block_alert_dialog)
            .show()
        val yesButton = alertDialog.findViewById<Button>(R.id.btnYes)
        val noButton = alertDialog.findViewById<MaterialButton>(R.id.btnNo)
        val userName = alertDialog.findViewById<TextView>(R.id.tvTitle)
        userName?.text = getString(R.string.block_username, thread?.otherUser)
        yesButton?.setOnClickListener {
            /* if (isInternetAvailable()) chatViewModel.blockUser(otherUserId!!)
             else showToast(requireContext(), getString(R.string.no_internet))
             handleBlockUser()
             alertDialog.dismiss()*/
        }
        noButton?.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun handleDeleteChatResponse() {
        viewModel.deleteChatRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.apply {
                        progressBar.remove()
                        messageRv.hide()
                        viewModel.allMessageList.clear()
                        noDataFound.root.show()
                        noDataFound.ivDataImg.setImageResource(R.drawable.ic_no_chat_messages)
                        noDataFound.tvTitle.text = getString(R.string.no_messages)
                        noDataFound.tvSubTitle.text = getString(R.string.no_chat_msg)
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

    private fun handleAllMessagesResponse() {
        viewModel.getMessagesRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()) {
                                messageRv.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.ic_no_chat_messages)
                                noDataFound.tvTitle.text = getString(R.string.no_messages)
                                noDataFound.tvSubTitle.text = getString(R.string.no_msg_body)
                            } else {
                                messageRv.show()
                                noDataFound.root.hide()
                                messageAdapter.saveData(viewModel.allMessageList)
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

    private fun handleApplicantMessagesResponse() {
        viewModel.getApplicantMessagesRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()) {
                                messageRv.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.ic_no_chat_messages)
                                noDataFound.tvTitle.text = getString(R.string.no_messages)
                                noDataFound.tvSubTitle.text = getString(R.string.no_msg_body)
                            } else {
                                messageRv.show()
                                noDataFound.root.hide()
                                messageAdapter.saveData(viewModel.allMessageList)
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

    override fun getMessageData(message: GetChatMessage) {
        requireActivity().runOnUiThread {
            viewModel.allMessageList.add(viewModel.allMessageList.size, message)
            binding.messageRv.scrollToPosition(messageAdapter.itemCount - 1)
            messageAdapter.saveData(viewModel.allMessageList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SocketHelper.disconnectSocket()
        SocketHelper.turnOffSocket()
    }
}