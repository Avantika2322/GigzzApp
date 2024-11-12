package com.gigzz.android.ui.auth

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentOtpVerificationBinding
import com.gigzz.android.databinding.FragmentRegisterBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import `in`.aabhasjindal.otptextview.OTPListener


class OtpVerificationFragment : Fragment(R.layout.fragment_otp_verification) {
    private val binding by viewBinding(FragmentOtpVerificationBinding::bind)
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        handleVerifyOtpResponse()
        setClickListener()
        handleSendOtpResponse()
    }

    private fun initViews() = with(binding) {
        enterOtp.text = getString(R.string.message_otp, authViewModel.emailId)
        val otp = arguments?.getString("otp").toString()
        if (otp.isNotEmpty()) {
            otpTextView.setOTP(otp)
            verifyMail.isEnabled = true
        }
        //registerForContextMenu(otpTextView)
        resendOtp.apply {
            val srcString = getString(R.string.resend_otp)
            val spannableString = SpannableString(srcString)
            val resendOtpClickableSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    authViewModel.sendOtpToUserApi(authViewModel.emailId)
                    /* if (requireContext().isInternetAvailable()) {
                         if (args.isFromForgotPassword) {
                             viewModel.sendForgotPassVerificationOtp(
                                 args.emailArg
                             )
                             handleOtpResponseFp()
                         } else {
                             viewModel.sendOtp(args.emailArg)
                             handleSendOtpResponse()
                         }
                     } else toast(R.string.no_internet)*/
                    p0.invalidate()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = ContextCompat.getColor(requireContext(), R.color.black)
                    resendOtp.invalidate()
                }
            }
            val resendOtpText = getString(R.string.resend_otp_text)
            val startIndex = srcString.lastIndexOf(resendOtpText)
            val endIndex = resendOtpText.length
            spannableString.setSpan(
                resendOtpClickableSpan,
                startIndex,
                startIndex + endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
        }

        otpTextView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                otpTextView.isHorizontalScrollBarEnabled = true
                verifyMail.isEnabled = false
            }

            override fun onOTPComplete(otp: String) {
                otpTextView.showSuccess()
                // otpTextView.hideKeyboard()
                verifyMail.isEnabled = true
            }
        }
    }

    private fun setClickListener() = binding.run {
        navigation.setOnClickListener {
            findNavController().popBackStack()
        }

        enterOtp.text = getString(R.string.message_otp, authViewModel.emailId)


        verifyMail.setOnClickListener {
            authViewModel.verifyOtpApi(authViewModel.emailId, otpTextView.otp.toString())
            /*if (isInternetAvailable()) {
                if (args.isFromForgotPassword) {
                    viewModel.verifyForgotPasswordOtp(
                        args.emailArg,
                        otpTextView.otp.toString()
                    )
                } else {
                    viewModel.verifyOtp(args.emailArg, otpTextView.otp.toString())
                }
            } else toast(R.string.no_internet)*/

        }
    }

    private fun handleVerifyOtpResponse() {
        authViewModel.verifyOtpResLiveData.observe(viewLifecycleOwner) { otpRes ->
            when (otpRes) {
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), otpRes.message!!)
                }

                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                is Resource.Success -> {
                    binding.progressBar.hide()
                    findNavController().navigate(R.id.action_otpVerificationFragment_to_createPasswordFragment)
                }
            }
        }
    }

    private fun handleSendOtpResponse() {
        authViewModel.sendOtpResLiveData.observe(viewLifecycleOwner) { otpRes ->
            when (otpRes) {
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), otpRes.message!!)
                }

                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                is Resource.Success -> {
                    binding.progressBar.hide()
                    otpRes.data?.data?.let {
                        binding.otpTextView.setOTP(it.otp)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.clearRes()
    }
}