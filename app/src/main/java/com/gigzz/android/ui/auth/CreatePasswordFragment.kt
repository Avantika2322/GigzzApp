package com.gigzz.android.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentCreatePasswordBinding
import com.gigzz.android.databinding.FragmentRegisterBinding
import com.gigzz.android.utils.showToast


class CreatePasswordFragment : Fragment(R.layout.fragment_create_password) {
    private val binding by viewBinding(FragmentCreatePasswordBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setClickListener()
       // handlePasswordReset()
    }

    private fun initViews() = with(binding) {
        confirmPassword.editText?.addTextChangedListener(textWatcher)
        enterPassword.editText?.addTextChangedListener(textWatcher)
    }

    private fun setClickListener() = binding.run {
        navigation.setOnClickListener {
            findNavController().popBackStack()
        }
        continueBtn.setOnClickListener {
            val password = enterPassword.editText?.text.toString()
            val confirmPassword = confirmPassword.editText?.text.toString()
            if (password == confirmPassword) {
                if (!isStrongPassword(password) && !isStrongPassword(confirmPassword)) {
                    val missingCriteria = mutableListOf<String>()
                    if (!password.any { it.isUpperCase() }) missingCriteria.add("uppercase letters")
                    if (!password.any { it.isLowerCase() }) missingCriteria.add("lowercase letters")
                    if (!password.any { it.isDigit() }) missingCriteria.add("numbers")
                    val specialCharacters = setOf('@', '#', '_')
                    if (!password.any { specialCharacters.contains(it) }) missingCriteria.add("special characters")
                    val message = "Create a Strong Password, minimum of 8 characters, with ${
                        missingCriteria.joinToString(",")
                    }."
                    showToast(requireContext(),message)
                } else {
                    findNavController().navigate(R.id.action_createPasswordFragment_to_userTypeFragment,
                        bundleOf("password" to password)
                    )
                }
            } else showToast(requireContext(),"Passwords do not match.")
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.continueBtn.isEnabled = false
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            validateFields()
        }
    }

    private fun isStrongPassword(password: String): Boolean {
        val minLength = 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { it == '@' || it == '#' || it == '_' }

        return password.length >= minLength &&
                hasUpperCase &&
                hasLowerCase &&
                hasDigit &&
                hasSpecialChar
    }

    private fun validateFields() {
        val password = binding.enterPassword.editText?.text.toString().trim()
        val confirmPassword = binding.confirmPassword.editText?.text.toString().trim()
        val isValidPassword = password.isNotEmpty() && confirmPassword.isNotEmpty()
        binding.continueBtn.isEnabled = isValidPassword
    }
}