package com.gigzz.android.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentForgotPasswordBinding


class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {
    private val binding by viewBinding(FragmentForgotPasswordBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}