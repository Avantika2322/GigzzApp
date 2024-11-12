package com.gigzz.android.ui.auth.jobseeker

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.databinding.FragmentIntrestsBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.ui.auth.OpportunitiesListAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.showToast
import com.google.android.material.chip.Chip


class InterestsFragment : Fragment() {
    private var _binding: FragmentIntrestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var interestsListAdapter: OpportunitiesListAdapter
    private val authViewModel: AuthViewModel by activityViewModels()
    private val interestList = ArrayList<String>()
    private val selectedInterest = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentIntrestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMasterDataObserver()

        binding.apply {
            interestsListAdapter =
                OpportunitiesListAdapter(interestList) { selectedItem, isChecked ->
                    onInterestClick(selectedItem, isChecked)
                }
            binding.recyclerViewInterests.adapter = interestsListAdapter

            interests.setOnClickListener {
                if (cardRecyclerInterest.isVisible) {
                    selectInterest.setImageResource(R.drawable.downarrow)
                    cardRecyclerInterest.isVisible = false
                } else {
                    selectInterest.setImageResource(R.drawable.arrow_up)
                    cardRecyclerInterest.isVisible = true
                }
            }

            toolProfile.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            continueBtn.setOnClickListener {
                if (selectedInterest.isNotEmpty()) {
                    authViewModel.jobSeeker.interests=selectedInterest
                    findNavController().navigate(R.id.action_interestsFragment_to_profileSetUpFragment,
                        bundleOf("from" to "jobSeeker")
                    )
                }

            }
        }
    }

    private fun onInterestClick(chipText: String, isChecked: Boolean) {
        if (isChecked) {
            val chip = Chip(requireContext())
            chip.text = chipText
            chip.isCloseIconVisible = true
            chip.setTextColor(requireActivity().getColor(R.color.theme_blue))
            chip.chipBackgroundColor =
                ColorStateList.valueOf(requireActivity().getColor(R.color.light_blue_shade_1))
            chip.tag = chipText
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                val tag = it.tag as? String
                tag?.let { removedItem ->
                    binding.chipGroupInterest.removeView(it)
                    selectedInterest.remove(removedItem)
                    val position = interestList.indexOf(removedItem)
                    interestsListAdapter.notifyItemInserted(interestList.size)
                    if (position != -1) {
                        interestsListAdapter.deselectItem(chipText)
                    }
                }
                checkAndToggleTextVisibility()
            }
            selectedInterest.add(chipText)
            binding.chipGroupInterest.addView(chip)
        }
        checkAndToggleTextVisibility()
    }

    private fun checkAndToggleTextVisibility() {
        binding.continueBtn.isEnabled = selectedInterest.isNotEmpty()
        binding.interestText.isVisible = binding.chipGroupInterest.childCount <= 0
        if (binding.chipGroupInterest.childCount <= 0) interestsListAdapter.clearAllSelections()
    }

    private fun getMasterDataObserver() {
        authViewModel.masterDataResLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    it.data?.data?.let {data->
                        if(!data.passion.isNullOrEmpty()) {
                            interestList.clear()
                            for (i in 0 until data.passion.size) {
                                if (!interestList.contains(data.passion[i].passion)) {
                                    interestList.add(data.passion[i].passion)
                                }
                            }
                            interestsListAdapter.notifyItemInserted(interestList.size)
                        }
                    }
                }

                is Resource.Error -> {}

                is Resource.InternetError -> {
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}