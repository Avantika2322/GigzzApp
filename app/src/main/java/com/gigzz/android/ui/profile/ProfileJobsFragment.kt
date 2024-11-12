package com.gigzz.android.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentProfileJobsBinding
import com.gigzz.android.domain.res.GetJobsRes
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.ui.jobs.CompanyJobsAdapter
import com.gigzz.android.ui.jobs.IndividualJobsAdapter
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileJobsFragment : Fragment(R.layout.fragment_profile_jobs) {
    private val binding by viewBinding(FragmentProfileJobsBinding::bind)
    private val profileViewModel by viewModels<ProfileViewModel>()
    private lateinit var jobsAdapter: IndividualJobsAdapter
    private var jobList: ArrayList<JobsData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getOtherUserJobsApi(OtherUserProfileFragment.userId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCompanyView()
        handleAllPostRes()
    }

    private fun initCompanyView() = binding.apply {
        jobsAdapter= IndividualJobsAdapter{pos, model, src -> }
        rvJobs.adapter = jobsAdapter
    }

    private fun handleAllPostRes() {
        profileViewModel.getOtherUserJobsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                rvJobs.hide()
                                noDataFound.root.show()
                                noDataFound.ivDataImg.setImageResource(R.drawable.no_post_found)
                                noDataFound.tvTitle.text=getString(R.string.no_jobs)
                                noDataFound.tvSubTitle.hide()
                            }else{
                                rvJobs.show()
                                noDataFound.root.hide()
                                jobList.clear()
                                jobList.addAll(data)
                                jobsAdapter.submitList(jobList)
                            }
                        }
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