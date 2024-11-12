package com.gigzz.android.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentEditSkillsBinding
import com.gigzz.android.domain.req.EditSkillReq
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.profile.adapter.EducationAdapter
import com.gigzz.android.ui.profile.adapter.SkillAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager


class EditSkillsFragment : Fragment(R.layout.fragment_edit_skills) {
    private val binding by viewBinding(FragmentEditSkillsBinding::bind)
    private var skillList = arrayListOf<String>()
    private var selectedSkillList = arrayListOf<String>()
    private lateinit var skillAdapter: SkillAdapter
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getMasterDataApi()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        handleMyProfileRes()
        handleMasterDataRes()
        clickListeners()
    }

    private fun initRecycler() = binding.apply {
        val flexboxLayoutManager = FlexboxLayoutManager(requireActivity())
        flexboxLayoutManager.apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        userSkillsRecycler.layoutManager = flexboxLayoutManager
        skillAdapter = SkillAdapter(skillList, selectedSkillList, false) { item, _ ->
            if (!selectedSkillList.contains(item)) {
                selectedSkillList.add(item)
            } else {
                selectedSkillList.remove(item)
            }
            skillAdapter.notifyDataSetChanged()
        }
        userSkillsRecycler.adapter = skillAdapter

    }

    private fun clickListeners() = binding.run {
        backView.setOnClickListener { findNavController().popBackStack() }

        saveBtn.setOnClickListener {
            profileViewModel.editSkillApi(EditSkillReq(selectedSkillList))
            handleEditSkillResponse()
        }
    }

    private fun handleMasterDataRes() {
        profileViewModel.getMasterRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    skillList.clear()
                    it.data?.data?.let { it ->
                        binding.apply {
                            it.let {
                                if (!it.skills.isNullOrEmpty()) {
                                    for (i in 0 until it.skills.size) {
                                        skillList.add(it.skills[i].skill_name)
                                    }
                                    skillAdapter.notifyDataSetChanged()
                                }
                            }
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

    private fun handleMyProfileRes() {
        profileViewModel.getMyProfileRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    selectedSkillList.clear()
                    it.data?.data?.let { it ->
                        binding.apply {
                            it.let {
                                selectedSkillList.addAll(it.skills)
                                //skillAdapter.notifyDataSetChanged()
                            }
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

    private fun handleEditSkillResponse() {
        profileViewModel.editUserProfileRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.remove()
                    findNavController().popBackStack()
                }

                is Resource.Error -> binding.progressBar.remove()
                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                is Resource.Loading -> binding.progressBar.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        profileViewModel.clearEditAddEduRes()
    }
}