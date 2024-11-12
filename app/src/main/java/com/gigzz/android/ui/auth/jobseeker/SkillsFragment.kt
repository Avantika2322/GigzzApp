package com.gigzz.android.ui.auth.jobseeker

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.databinding.FragmentSkillsBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.ui.auth.OpportunitiesListAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.showToast
import com.google.android.material.chip.Chip


class SkillsFragment : Fragment() {
    private var _binding: FragmentSkillsBinding? = null
    private val binding get() = _binding!!
    private lateinit var skillsListAdapter: OpportunitiesListAdapter
    private val authViewModel: AuthViewModel by activityViewModels()
    private val skillList = ArrayList<String>()
    private val selectedSkills = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMasterDataObserver()

        binding.apply {
            skillsListAdapter =
                OpportunitiesListAdapter(skillList) { selectedItem, isChecked ->
                    onSkillClickChip(selectedItem, isChecked)
                }
            recyclerViewSkills.adapter = skillsListAdapter

            skills.setOnClickListener {
                if (cardRecyclerSkills.isVisible) {
                    selectSkills.setImageResource(R.drawable.downarrow)
                    cardRecyclerSkills.isVisible = false
                } else {
                    selectSkills.setImageResource(R.drawable.arrow_up)
                    cardRecyclerSkills.isVisible = true
                }
            }

            toolProfile.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            continueBtn.setOnClickListener {
                if (selectedSkills.isNotEmpty()) {
                    authViewModel.jobSeeker.skills = selectedSkills
                    findNavController().navigate(R.id.action_skillsFragment_to_interestsFragment)
                } else showToast(requireContext(), "Select at least 1 Skill")
            }
        }

    }

    private fun onSkillClickChip(chipText: String, isChecked: Boolean) {
        if (isChecked) {
            val chip = Chip(requireContext())
            chip.text = chipText
            chip.tag = chipText
            chip.isCloseIconVisible = true
            chip.setTextColor(requireActivity().getColor(R.color.theme_blue))
            chip.chipBackgroundColor =
                ColorStateList.valueOf(requireActivity().getColor(R.color.light_blue_shade_1))
            chip.setOnCloseIconClickListener {
                val tag = it.tag as? String
                tag?.let { removedItem ->
                    binding.chipGroupSkills.removeView(it)
                    selectedSkills.remove(removedItem)
                    val position = skillList.indexOf(removedItem)
                    skillsListAdapter.notifyItemInserted(skillList.size)
                    if (position != -1) {
                        skillsListAdapter.deselectItem(chipText)
                    }
                }
                checkAndToggleTextVisibility()
            }
            selectedSkills.add(chipText)
            binding.chipGroupSkills.addView(chip)
        }
        checkAndToggleTextVisibility()
    }

    private fun checkAndToggleTextVisibility() {
        binding.continueBtn.isEnabled = selectedSkills.isNotEmpty()
        binding.skillsText.isVisible = binding.chipGroupSkills.childCount <= 0
        if (binding.chipGroupSkills.childCount <= 0) skillsListAdapter.clearAllSelections()
    }

    private fun getMasterDataObserver() {
        authViewModel.masterDataResLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    it.data?.data?.let {data->
                        if(!data.skills.isNullOrEmpty()) {
                            skillList.clear()
                            for (i in 0 until data.skills.size) {
                                if (!skillList.contains(data.skills[i].skill_name)) {
                                    skillList.add(data.skills[i].skill_name)
                                }
                            }
                            skillsListAdapter.notifyItemInserted(skillList.size)
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