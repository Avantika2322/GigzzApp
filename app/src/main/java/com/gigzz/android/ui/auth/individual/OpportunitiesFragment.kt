package com.gigzz.android.ui.auth.individual

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentOpportunitiesBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.ui.auth.OpportunitiesListAdapter
import com.gigzz.android.ui.home.HomeActivity
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.chip.Chip


class OpportunitiesFragment : Fragment(R.layout.fragment_opportunities) {
    private val binding by viewBinding(FragmentOpportunitiesBinding::bind)
    private val viewModel: AuthViewModel by activityViewModels()
    private lateinit var opportunitiesListAdapter: OpportunitiesListAdapter
    private val version = arrayOf(
        "BabySitting", "Cooking", "PetCare", "Painter", "Miscellaneous"
    )
    private val selectedOpportunities = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        opportunitiesListAdapter =
            OpportunitiesListAdapter(version.toList()) { selectedItem, isChecked ->
                addChip(selectedItem, isChecked)
            }
        binding.recyclerView.adapter = opportunitiesListAdapter

        setClickListener()
        handleIndividualSignUp()
    }

    private fun setClickListener() = binding.run {
        profileToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        opportunity.setOnClickListener {
            if (cardRecycler.isVisible) {
                selectOpportunities.setImageResource(R.drawable.downarrow)
                cardRecycler.isVisible = false
            } else {
                selectOpportunities.setImageResource(R.drawable.arrow_up)
                cardRecycler.isVisible = true
            }

        }
        continueBtn.setOnClickListener {
            viewModel.individualJobGiverReq.opportunities = selectedOpportunities
            viewModel.individualJobGiverReq.emailId = viewModel.emailId
            viewModel.signUpAsIndividualApi()
        }
    }

    private fun addChip(chipText: String, isChecked: Boolean) {
        if (isChecked) {
            val chip = Chip(requireContext())
            chip.text = chipText
            chip.tag = chipText
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                val tag = it.tag as? String
                tag?.let { removedItem ->
                    binding.chipGroup.removeView(it)
                    selectedOpportunities.remove(removedItem)
                    val position = version.indexOf(removedItem)
                    if (position != -1) {
                        opportunitiesListAdapter.deselectItem(chipText)
                    }
                }
                checkAndToggleTextVisibility()
            }
            selectedOpportunities.add(chipText)
            binding.chipGroup.addView(chip)
        }
        checkAndToggleTextVisibility()
    }

    private fun checkAndToggleTextVisibility() {
        binding.apply {
            continueBtn.isEnabled = selectedOpportunities.isNotEmpty()
            opportunityText.isVisible = chipGroup.childCount <= 0
            if (chipGroup.childCount <= 0) opportunitiesListAdapter.clearAllSelections()
        }
    }

    private fun handleIndividualSignUp() {
        viewModel.signUpAsIndividualResLiveData.observe(viewLifecycleOwner) { response ->
            when(response){
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Success -> {
                    response.data?.data?.let { data ->
                        binding.progressBar.hide()
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        intent.putExtra("userType", data.userType)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), response.message.toString())
                }
                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }
            }
        }
    }
}