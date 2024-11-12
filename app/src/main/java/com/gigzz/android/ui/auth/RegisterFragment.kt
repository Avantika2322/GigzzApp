package com.gigzz.android.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentRegisterBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.isEmailValid
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast


class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val binding by viewBinding(FragmentRegisterBinding::bind)
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        handleSendOtpResponse()
        setClickListener()
    }

    private fun initViews() = with(binding) {
        edtMail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                registerBtn.isEnabled = false
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val enteredMail = p0.toString()
                if (enteredMail.isNotEmpty() and enteredMail.isEmailValid()) {
                    registerBtn.isEnabled = true
                } else if (!enteredMail.isEmailValid()) {
                    registerBtn.isEnabled = false
                }
            }
        })

        signInText.apply {
            val signInStr = getString(R.string.already_have_account)
            val signInSpannableStr = SpannableString(signInStr)
            val signInClickSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    findNavController().popBackStack()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = ContextCompat.getColor(requireContext(), R.color.black)
                }
            }

            val strSignIn = getString(R.string.sign_in_text)
            val startIndex = signInStr.lastIndexOf(strSignIn)
            val endIndex = strSignIn.length
            signInSpannableStr.setSpan(
                signInClickSpan,
                startIndex,
                startIndex + endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = signInSpannableStr
            movementMethod = LinkMovementMethod.getInstance()
        }

        termsConditionsText.apply {
            val srcStringTerms = getString(R.string.privacy_policy)
            val stringSpan = SpannableString(srcStringTerms)

            val termsClickSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onLinkClick(AppConstants.TNC_URL)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                }
            }

            val privacyPolicyClickSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onLinkClick(AppConstants.PRIVACY_URL)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                }
            }

            val strTerms = getString(R.string.terms)
            val strPrivacyPolicy = getString(R.string.privacy_policy_text)
            val startPosTerms = srcStringTerms.lastIndexOf(strTerms)
            val endPosTerms = strTerms.length
            val startPosPrivacyPolicy = srcStringTerms.lastIndexOf(strPrivacyPolicy)
            val endPosPrivacyPolicy = strPrivacyPolicy.length
            stringSpan.setSpan(
                termsClickSpan,
                startPosTerms,
                startPosTerms + endPosTerms,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            stringSpan.setSpan(
                privacyPolicyClickSpan,
                startPosPrivacyPolicy,
                startPosPrivacyPolicy + endPosPrivacyPolicy,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = stringSpan
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun onLinkClick(myUrl: String) {
        var url = myUrl
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun setClickListener() = binding.run {
        registerBtn.setOnClickListener {
            authViewModel.sendOtpToUserApi(edtMail.editText?.text.toString())
        }

        ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleSendOtpResponse() {
        authViewModel.sendOtpResLiveData.observe(viewLifecycleOwner) { otpRes ->
            when(otpRes){
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(),otpRes.message!!)
                }
                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(),getString(R.string.no_internet))
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    val email = binding.edtMail.editText?.text?.trim().toString()
                    authViewModel.emailId = email
                    otpRes.data?.data.let {
                        if (it!=null) {
                            findNavController().navigate(
                                R.id.action_registerFragment_to_otpVerificationFragment,
                                bundleOf("otp" to it.otp)
                            )
                        }
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