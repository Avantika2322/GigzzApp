package com.gigzz.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentSignInBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.ui.home.HomeActivity
import com.gigzz.android.ui.onboarding.MainActivity
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.isEmailValid
import com.gigzz.android.utils.showToast
import com.gigzz.android.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {
    private val binding by viewBinding(FragmentSignInBinding::bind)
    private val authViewModel by viewModels<AuthViewModel>()
    private val handler = Handler(Looper.getMainLooper())
    private var isButtonClickEnabled = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setClickListener()
        signInObserver()
       // authViewModel.demoApi()
    }

    private fun setClickListener() = binding.run {
        signInBtn.setOnClickListener {
            if (isButtonClickEnabled) {
                isButtonClickEnabled = false
                authViewModel.signInUser(
                    emailId = edtMail.editText?.text?.trim().toString(),
                    password = enterPassword.editText?.text?.trim().toString()
                )
                handler.postDelayed({ isButtonClickEnabled = true }, 5000)
            }
        }
        forgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_forgotPasswordFragment)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finish()
        }
    }

    private fun initViews() = with(binding) {
        edtMail.editText?.addTextChangedListener(textWatcher)
        enterPassword.editText?.addTextChangedListener(textWatcher)
        textRegister.apply {
            val srcString = getString(R.string.not_having_account)
            val spannableString = SpannableString(srcString)
            val registerClickSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    //val action = SignInFragmentDirections.actionSignInFragmentToRegisterFragment()
                    findNavController().navigate(R.id.action_signInFragment_to_registerFragment)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = ContextCompat.getColor(requireContext(), R.color.black)
                }
            }
            val strRegister = getString(R.string.register_now)
            val startIndex = srcString.lastIndexOf(strRegister)
            val endIndex = strRegister.length
            spannableString.setSpan(
                registerClickSpan,
                startIndex,
                startIndex + endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()

        }
    }

    /* private fun handleSignInRes() {
         authViewModel.signInResLiveData.observe(viewLifecycleOwner) { signInRes ->
             if (signInRes != null) {
                 signInRes.data?.let {
                     val intent = Intent(requireActivity(), MainActivity::class.java)
                     intent.putExtra("userType", signInRes.data.userType)
                     startActivity(intent)
                     requireActivity().finish()
                 }
             }
         }
     }*/


    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.signInBtn.isEnabled = false
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            validateFields()
        }
    }

    private fun validateFields() {
        val email = binding.edtMail.editText?.text.toString().trim()
        val password = binding.enterPassword.editText?.text.toString().trim()
        val isEmailValid = email.isNotEmpty() && email.isEmailValid()
        val allFieldsFilled = isEmailValid && password.isNotEmpty()
        binding.signInBtn.isEnabled = allFieldsFilled

    }

    private fun signInObserver() {
        authViewModel.signInResLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    it.data?.data?.let { data ->
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        intent.putExtra("userType", data.userType)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }

                is Resource.Error -> {
                    showToast(requireContext(), it.message.toString())
                }

                is Resource.InternetError -> {
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {}
            }
        }
    }
}