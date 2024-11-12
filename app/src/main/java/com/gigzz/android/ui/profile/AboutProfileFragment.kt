package com.gigzz.android.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentAboutProfileBinding
import com.gigzz.android.domain.res.EducationDetailsData
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.profile.adapter.EducationAdapter
import com.gigzz.android.ui.profile.adapter.SkillAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager


class AboutProfileFragment : Fragment(R.layout.fragment_about_profile) {
    private val binding by viewBinding(FragmentAboutProfileBinding::bind)
    private var skillList= arrayListOf<String>()
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)
    private var resumeUrl = ""
    private var name = ""
    private lateinit var skillAdapter: SkillAdapter
    private lateinit var educationAdapter: EducationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(profileViewModel.userType!= 1){
            binding.cardSkills.remove()
            binding.cardResume.remove()
            binding.cardEducation.remove()
        }
        initRecycler()
        handleMyProfileRes()
        handleEducationRes()
        clickListeners()
    }

    private fun initRecycler() = binding.apply {
        val flexboxLayoutManager = FlexboxLayoutManager(requireActivity())
        flexboxLayoutManager.apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        rvSkills.layoutManager = flexboxLayoutManager
        skillAdapter = SkillAdapter(skillList, emptyList(), true) { _, _ -> }
        rvSkills.adapter = skillAdapter

        educationAdapter=EducationAdapter(profileViewModel.educationList){model,pos-> }
        rvEducation.adapter=educationAdapter
    }

    private fun clickListeners() = binding.run {
        ivEditEducation.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_allEducationListFragment)
        }

        ivEditSkills.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editSkillsFragment)
        }

    }

    private fun handleMyProfileRes() {
        profileViewModel.getMyProfileRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    skillList.clear()
                    it.data?.data?.let { it ->
                        binding.apply {
                            resumeUrl = it.resumeUrl.toString()
                            name = it.fullName.toString()
                            tvOverview.text = it.bio
                            tvLocation.text = it.address
                            //etLocation.text = it?.address?.substringAfterLast(",")
                            tvContact.text = it.phoneNumber
                            //userEmail.text = it?.emailId
                            it.let {
                                skillList.addAll(it.skills)
                                skillAdapter.notifyDataSetChanged()
                            }
                            if (!it.resumeUrl.isNullOrEmpty()) tvResumeTitle.text = it.resumeUrl.toString().substringAfterLast("/")
                            else cardResume.remove()
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

    private fun handleEducationRes() {
        profileViewModel.getEducationRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    it.data?.data?.let { data ->
                        educationAdapter.notifyDataSetChanged()
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

}