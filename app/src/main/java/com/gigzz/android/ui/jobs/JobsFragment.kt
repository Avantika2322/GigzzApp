package com.gigzz.android.ui.jobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentCreatePasswordBinding
import com.gigzz.android.databinding.FragmentJobsBinding
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.loadImage
import com.gigzz.android.utils.loadImageFromS3
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch


class JobsFragment : Fragment(R.layout.fragment_jobs) {
    private val binding by viewBinding(FragmentJobsBinding::bind)
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private lateinit var companyJobsAdapter: CompanyJobsAdapter
    private lateinit var individualJobsAdapter: IndividualJobsAdapter

    private var page = 1

    companion object{
        var userType: Int? = 0
        var jobType: Int? = 0
        var imageUrl: String? = ""
        var companyName: String? = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val profileRes = jobsViewModel.getProfileRes()
            userType = profileRes.userType
            imageUrl = profileRes.profileImageUrl
            companyName = profileRes.companyName
        }
        if (userType==2) jobsViewModel.getAllIndividualJobs()
        else if (userType==3) jobsViewModel.getAllCompanyJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setOnClickListeners()
        initCompanyView()
    }

    private fun initCompanyView() = binding.apply {
        if (userType==2) {
            individualJobsAdapter = IndividualJobsAdapter { pos, model, src ->
                when (src) {
                    "root" -> {
                        findNavController().navigate(
                            R.id.action_jobsFragment_to_postedJobsDetailFragment,
                            bundleOf("jobId" to model.jobId, "pos" to pos)
                        )
                        individualJobsAdapter.submitList(jobsViewModel.individualJobList)
                    }
                }
            }
            companyJobs.rvJobs.adapter = individualJobsAdapter
        }else{
            companyJobsAdapter = CompanyJobsAdapter() { pos, model, src ->
                when (src) {
                    "root" -> {
                        findNavController().navigate(
                            R.id.action_jobsFragment_to_postedJobsDetailFragment,
                            bundleOf("jobId" to model.jobId, "pos" to pos)
                        )
                        companyJobsAdapter.submitList(jobsViewModel.companyJobList)
                    }
                }
            }
            companyJobs.rvJobs.adapter = companyJobsAdapter
        }

        topView.userDp.loadImageFromS3(
            imageUrl,
            R.drawable.user_placeholder
        )
    }

    private fun setOnClickListeners() = binding.run {

        topView.notificationIv.setOnClickListener {
           // findNavController().navigate(R.id.action_jobsTabLayoutFragment_to_notificationFragment)
        }

        topView.imageContainer.setOnClickListener {
           // findNavController().navigate(R.id.action_jobsTabLayoutFragment_to_profileFragment)
        }

        topView.userDp.setOnClickListener {
            findNavController().navigate(R.id.action_jobsFragment_to_my_profile_nav_graph)
        }

        topView.searchView.setOnClickListener {
           /* findNavController().navigate(
                JobsTabLayoutFragmentDirections.actionJobsTabLayoutFragmentToSearchFragment(
                    isFromFrag = "Job"
                )
            )*/
        }

        companyJobs.chipCompany.setOnClickListener {
            if (userType==2) findNavController().navigate(R.id.action_jobsFragment_to_postEditIndividualJobFragment)
            else findNavController().navigate(R.id.action_jobsFragment_to_postEditNewJobFragment)
        }

        topView.ivSearch.setOnClickListener {
            /*findNavController().navigate(
                JobsTabLayoutFragmentDirections.actionJobsTabLayoutFragmentToSearchFragment(
                    isFromFrag = "Job"
                )
            )*/
        }

        companyJobs.ivFilter.setOnClickListener {
            if (userType==2) {
                companyJobs.ivFilter.isClickable = false
                val dialogFragment = JobFilterBottomFragment("individual")
                dialogFragment.show(childFragmentManager, "bottom")
                companyJobs.ivFilter.isClickable = true
            }else {
                companyJobs.ivFilter.isClickable = false
                val dialogFragment = JobFilterBottomFragment("company")
                dialogFragment.show(childFragmentManager, "bottom")
                companyJobs.ivFilter.isClickable = true
            }
        }
    }

    private fun initViews() = with(binding) {
        if (userType == 1) {
            topView.searchView.remove()
            topView.ivSearch.show()
            companyJobs.root.remove()
            viewPagerTab.show()
            viewPagerScreen.show()
        }else{
            topView.searchView.show()
            topView.ivSearch.hide()
            companyJobs.root.show()
            viewPagerTab.remove()
            companyJobs.ivFilter.remove()
            viewPagerScreen.remove()
            if(userType==2) handleAllIndividualJobRes()
            else handleAllCompanyJobRes()
        }

        jobType = when (userType) {
            1 -> 0
            2 -> 1
            3 -> 2
            else -> 0
        }
        viewPagerScreen.adapter =ScreenChangerViewPagerAdapter(this@JobsFragment)

        TabLayoutMediator(
            viewPagerTab, binding.viewPagerScreen
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.text = "All Jobs"
                1 -> tab.text = "Applied Jobs"
                2 -> tab.text = "My Jobs"
            }
        }.attach()
        viewPagerScreen.isUserInputEnabled = false
    }

    private fun handleAllIndividualJobRes() {
        jobsViewModel.getAllIndividualJobsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.apply {
                        binding.progressBar.remove()
                        it.data?.data?.let { data ->
                            binding.apply {
                                if (data.isEmpty()) {
                                    companyJobs.rvJobs.hide()
                                    companyJobs.noDataFound.root.show()
                                    companyJobs.noDataFound.tvTitle.text = getString(R.string.no_jobs)
                                    companyJobs.noDataFound.tvSubTitle.hide()
                                } else {
                                    companyJobs.rvJobs.show()
                                    companyJobs.noDataFound.root.remove()
                                    companyJobs.ivFilter.show()
                                    individualJobsAdapter.submitList(jobsViewModel.individualJobList)
                                }
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

    private fun handleAllCompanyJobRes() {
        jobsViewModel.getAllCompanyJobsRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.apply {
                        binding.progressBar.remove()
                        it.data?.data?.let { data ->
                            binding.apply {
                                if (data.isEmpty()) {
                                    companyJobs.rvJobs.hide()
                                    companyJobs.noDataFound.root.show()
                                    companyJobs.noDataFound.tvTitle.text = getString(R.string.no_jobs)
                                    companyJobs.noDataFound.tvSubTitle.hide()
                                } else {
                                    companyJobs.rvJobs.show()
                                    companyJobs.noDataFound.root.remove()
                                    companyJobs.ivFilter.show()
                                    companyJobsAdapter.submitList(jobsViewModel.companyJobList)
                                }
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

    inner class ScreenChangerViewPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AllJobsFragment()
                1 -> AppliedJobsFragment()
                2 -> MyJobsFragment()
                else -> AllJobsFragment()
            }
        }

        override fun getItemCount(): Int {
            return 3
        }
    }

    /*override fun onResume() {
        super.onResume()
        if (jobsViewModel.editedJobId != -1) {
            for ((index, i) in jobList.withIndex()) {
                if (i.jobId == viewModel.editedJobId) {
                    jobList[index] = i.copy(bookmarkStatus = 0)
                    break
                }
            }
            viewModel.editedJobId = -1
            adapter.submitList(jobList)
        }
    }*/
}