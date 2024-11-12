package com.gigzz.android.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentAllEducationListBinding
import com.gigzz.android.domain.res.EducationDetailsData
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.profile.adapter.AllEducationAdapter
import com.gigzz.android.ui.profile.adapter.EducationAdapter
import com.gigzz.android.ui.profile.adapter.SkillAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast

class AllEducationListFragment : Fragment(R.layout.fragment_all_education_list) {
    private val binding by viewBinding(FragmentAllEducationListBinding::bind)
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)
    private lateinit var educationAdapter: AllEducationAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        educationAdapter= AllEducationAdapter(profileViewModel.educationList){ model, pos, src->
            when(src){
                "new"-> {
                    if (model==null) findNavController().navigate(R.id.action_allEducationListFragment_to_editEducationFragment,
                        bundleOf("model" to null,"edit" to false)
                    )
                }

                "edit"->{
                    findNavController().navigate(R.id.action_allEducationListFragment_to_editEducationFragment,
                        bundleOf("model" to model,"edit" to true)
                    )
                }
            }

        }
        binding.rvEducation.adapter=educationAdapter

        clickListeners()
        handleEducationRes()
    }

    private fun clickListeners() = binding.run {
        ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        /*ivEditEducation.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_allEducationListFragment)
        }*/
    }

    private fun handleEducationRes() {
        profileViewModel.getEducationRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        educationAdapter.notifyDataSetChanged()
                    }
                }

                is Resource.Error -> binding.progressBar.remove()

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> binding.progressBar.remove()
            }
        }
    }
}