package com.gigzz.android.ui.jobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentAllJobsBinding
import com.gigzz.android.databinding.FragmentAppliedJobsBinding
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast


class AppliedJobsFragment : Fragment(R.layout.fragment_applied_jobs) {
    private val binding by viewBinding(FragmentAppliedJobsBinding::bind)
    private val viewModel: JobsViewModel by activityViewModels()
    private lateinit var adapter: AppliedJobsAdapter
    private val jobList = ArrayList<JobsData>()
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        viewModel.getMyAppliedJobs()
        handleAllJobRes()

        binding.jobFilter.setOnClickListener {
            binding.jobFilter.isClickable = false
            val dialogFragment = JobFilterBottomFragment("applied")
            dialogFragment.show(childFragmentManager, "bottom")
            binding.jobFilter.isClickable = true
        }
    }

    private fun initViews() = with(binding) {
        adapter=AppliedJobsAdapter { pos, model, src ->
            when (src) {
                "root" -> {
                    findNavController().navigate(
                        R.id.action_jobsFragment_to_jobDetailsFragment,
                        bundleOf("jobData" to model)
                    )
                    adapter.submitList(jobList)
                }
            }
        }
        postedJobRv.adapter=adapter
        (binding.postedJobRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        postedJobRv.setItemAnimator(null)
    }

    private fun handleAllJobRes() {
        viewModel.getMyAppliedJobsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                postedJobRv.hide()
                                jobFilter.hide()
                                binding.noChats.show()
                            }else{
                                postedJobRv.show()
                                jobFilter.show()
                                binding.noChats.remove()
                                jobList.clear()
                                jobList.addAll(data)
                                adapter.submitList(jobList)
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