package com.gigzz.android.ui.auth

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentUserTypeBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.utils.show


class UserTypeFragment : Fragment(R.layout.fragment_user_type) {
    private val binding by viewBinding(FragmentUserTypeBinding::bind)
    private val authViewModel by activityViewModels<AuthViewModel>()
    private var selectedItem = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setOnClickListener()
    }

    private fun initViews() = with(binding) {
        val cardLayouts = listOf(card1, card2, card3)
        val cardTextViews = listOf(cardText1, cardText2, cardText3)
        val cardDescTv = listOf(card1Desc, card2Desc, card3Desc)
        cardLayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                continueBtn.show()
                resetCardColors(cardLayouts, cardTextViews, cardDescTv)
                layout.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.theme_blue))
                cardTextViews[index].setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
                cardDescTv[index].setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
                selectedItem = index
            }
        }
    }

    private fun setOnClickListener() = binding.run {
        continueBtn.setOnClickListener {
            when (selectedItem) {
                0 -> {
                    authViewModel.jobSeeker.password=arguments?.getString("password")
                    findNavController().navigate(R.id.action_userTypeFragment_to_gigzzUserFragment)
                }

                1 -> {
                    authViewModel.individualJobGiverReq.password=arguments?.getString("password")
                    findNavController().navigate(R.id.action_userTypeFragment_to_individualJobGiverProfileFragment)
                }

                2 -> {
                    authViewModel.companyReq.password=arguments?.getString("password")
                    findNavController().navigate(R.id.action_userTypeFragment_to_companyJobGiverProfileFragment)
                }
            }
        }
    }

    private fun resetCardColors(
        cardLayouts: List<CardView>,
        cardTextViews: List<TextView>,
        cardDescTv: List<TextView>
    ) {
        cardLayouts.forEach { layout ->
            layout.background.clearColorFilter()
            layout.backgroundTintList = null
        }

        cardTextViews.forEach { textView ->
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        cardDescTv.forEach { textView ->
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_shade_1))
        }

    }
}