package com.gigzz.android.ui.jobs

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentMyJobsBinding
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast


class MyJobsFragment : Fragment(R.layout.fragment_my_jobs) {
    private val binding by viewBinding(FragmentMyJobsBinding::bind)
    private val viewModel: JobsViewModel by activityViewModels()
    private lateinit var adapter: MyJobsAdapter
    private val jobList = ArrayList<JobsData>()
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getMyBookmarkedJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        handleAllJobRes()
    }

    private fun initViews() = with(binding) {
        jobFilter.hide()
        adapter=MyJobsAdapter { pos, model, src ->
            when(src){
                "remove"->{
                    jobList.removeAt(pos)
                    viewModel.removeBookmarkJobApi(model.jobId,"myJobs")
                    adapter.submitList(jobList)
                }
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
    }

    private fun handleAllJobRes() {
        viewModel.getAllBookmarkedJobsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()){
                                postedJobRv.hide()
                                binding.noChats.show()
                            }else{
                                postedJobRv.show()
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