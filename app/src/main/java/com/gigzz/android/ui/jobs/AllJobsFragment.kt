package com.gigzz.android.ui.jobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentAllJobsBinding
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast


class AllJobsFragment : Fragment(R.layout.fragment_all_jobs) {
    private val binding by viewBinding(FragmentAllJobsBinding::bind)
    private val viewModel: JobsViewModel by activityViewModels()
    private lateinit var adapter: AllJobsAdapter
    private val jobList = ArrayList<JobsData>()
    private var page = 1

    //eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbF9pZCI6ImhlbHBpbmdoYW5kQHlvcG1haWwuY29tIiwiaWF0IjoxNzE0NzMzMjI5fQ.It2wXFzckXiGUButjKBdKgVtOXkuADOHIuxo2Vlyztg
    //eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbF9pZCI6ImF2YW50aWthLnJhb0BhZ2ljZW50LmluIiwiaWF0IjoxNzE1NTgzMzY1fQ.qtAKacGGf5LS_9HI6bnHijT4UfU3cD-fxq_OuFyUZ8c

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllJobs(page, JobsFragment.jobType!!)
        initViews()
        handleAllJobRes()

        binding.jobFilter.setOnClickListener {
            binding.jobFilter.isClickable = false
            val dialogFragment = JobFilterBottomFragment("all")
            dialogFragment.show(childFragmentManager, "bottom")
            binding.jobFilter.isClickable = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.editedJobId != -1) {
            for ((index, i) in jobList.withIndex()) {
                if (i.jobId == viewModel.editedJobId) {
                    jobList[index] = i.copy(bookmarkStatus = 0)
                    break
                }
            }
            viewModel.editedJobId = -1
            adapter.submitList(jobList)
        }
    }

    private fun initViews() = with(binding) {
        refreshLayout.setOnRefreshListener {
            viewModel.getAllJobs(1, JobsFragment.jobType!!)
            refreshLayout.isRefreshing = false
        }

        adapter = AllJobsAdapter { pos, model, src ->
            when (src) {
                "add" -> {
                    jobList[pos].let {
                        jobList[pos] = it.copy(bookmarkStatus = 1)
                    }
                    viewModel.bookmarkJobApi(model.jobId)
                    adapter.submitList(jobList)
                }

                "remove" -> {
                    jobList[pos].let {
                        jobList[pos] = it.copy(bookmarkStatus = 0)
                    }
                    viewModel.removeBookmarkJobApi(model.jobId, "allJobs")
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
        rvAllJobs.adapter = adapter
        (binding.rvAllJobs.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvAllJobs.setItemAnimator(null)

    }

    private fun handleAllJobRes() {
        viewModel.getAllJobsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    it.data?.data?.let { data ->
                        binding.apply {
                            if (data.isEmpty()) {
                                rvAllJobs.hide()
                                jobFilter.hide()
                                //   binding.noChats.show()
                            } else {
                                rvAllJobs.show()
                                jobFilter.show()
                                //  binding.noChats.remove()
                                if (page == 1) {
                                    jobList.clear()
                                }
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