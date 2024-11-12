package com.gigzz.android.ui.jobs

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
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentPostedJobsDetailBinding
import com.gigzz.android.domain.res.CompanyJobData
import com.gigzz.android.domain.res.JobDataById
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.formatDateTime
import com.gigzz.android.utils.getCombinedDateWithStartAndEndDate
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.loadImage
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast


class PostedJobsDetailFragment : Fragment(R.layout.fragment_posted_jobs_detail) {
    private val binding by viewBinding(FragmentPostedJobsDetailBinding::bind)
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    var jobId: Int? = -1
    var position: Int? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            jobId = getInt("jobId")
            position = getInt("pos")
        }

        jobId?.let {
            if (JobsFragment.userType == 2) jobsViewModel.getPostedJobById(it)
            else jobsViewModel.getCompanyPostedJobById(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
        if (JobsFragment.userType == 2) handleJobDetailRes()
        else handleCompanyJobDetailRes()
    }

    private fun clickListeners() = binding.run {
        ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        ivEdit.setOnClickListener {
            if (JobsFragment.userType == 2) {
                findNavController().navigate(
                    R.id.action_postedJobsDetailFragment_to_postEditIndividualJobFragment,
                    bundleOf("isFromEdit" to true, "jobId" to jobId)
                )
            } else findNavController().navigate(
                R.id.action_postedJobsDetailFragment_to_postEditNewJobFragment,
                bundleOf("isFromEdit" to true, "jobId" to jobId)
            )
        }

        iv.setOnClickListener {
            if (jobId != null) {
                jobsViewModel.createPostFromJob(jobId!!)
                handleCreatePostFromJobRes()
            } else showToast(requireContext(), getString(R.string.something_went_wro))
        }

        ivDelete.setOnClickListener {
            if (jobId != null) {
                jobsViewModel.deleteJob(jobId!!)
                handleDeleteJobRes()
            }
        }
    }

    private fun handleJobDetailRes() {
        jobsViewModel.getPostedJobByIdRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {
                    binding.cardDetail.hide()
                    binding.progressBar.show()
                }

                is Resource.Success -> {
                    binding.cardDetail.show()
                    binding.apply {
                        binding.progressBar.remove()
                        it.data?.data?.let { data ->
                            binding.apply {
                                setDataOnUi(data)
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    binding.cardDetail.hide()
                    binding.progressBar.remove()
                }

                is Resource.InternetError -> {
                    binding.cardDetail.hide()
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {
                    binding.cardDetail.hide()
                    binding.progressBar.remove()
                }
            }
        }
    }

    private fun handleCompanyJobDetailRes() {
        jobsViewModel.getCompanyPostedJobByIdRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {
                    binding.cardDetail.hide()
                    binding.progressBar.show()
                }

                is Resource.Success -> {
                    binding.cardDetail.show()
                    binding.apply {
                        binding.progressBar.remove()
                        it.data?.data?.let { data ->
                            binding.apply {
                                setCompanyJobDataOnUi(data)
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    binding.cardDetail.hide()
                    binding.progressBar.remove()
                }

                is Resource.InternetError -> {
                    binding.cardDetail.hide()
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {
                    binding.cardDetail.hide()
                    binding.progressBar.remove()
                }
            }
        }
    }

    private fun setCompanyJobDataOnUi(it: CompanyJobData) = binding.apply {
        tvJobTitle.text = it.company_name
        if (it.images.isNotEmpty()) {
            it.images[0].let { img ->
                ivPostImg.loadImage(
                    img.imageUrl,
                    R.drawable.post_placeholder
                )
            }
        } else ivPostImg.remove()

        tvAddress.text = it.address
        tvSalary.text = getString(
            R.string.total_salary,
            it.salary
        )

        tvJobDesc.text=it.description

        if (it.experience.isNotEmpty()) {
            layoutExperience.show()
            tvExperience.text = it.experience
        }else layoutExperience.remove()

        when (it.job_type) {
            1 -> tvJobType.text = getString(R.string.full_time)
            2 -> tvJobType.text = getString(R.string.part_time)
            3 -> tvJobType.text = getString(R.string.both)
            else->layoutJobType.remove()
        }
    }

    private fun handleCreatePostFromJobRes() {
        jobsViewModel.postEditJobRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.apply {
                        binding.progressBar.remove()
                        it.data?.let { data ->
                            if (data.message == "Success") {
                                homeViewModel.getAllPosts(1)
                                showToast(requireContext(), data.message)
                            } else showToast(requireContext(), data.message)
                        }
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), it.message.toString())
                }

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> binding.progressBar.remove()
            }
        }
    }

    private fun setDataOnUi(it: JobDataById) = binding.apply {
        tvJobTitle.text = it.jobName
        if (it.images?.isNotEmpty() == true) {
            it.images[0].let { img ->
                img.cachedImgThumbnailUrl = ivPostImg.loadCachedImg(
                    img.imageThumbnail,
                    img.cachedImgThumbnailUrl,
                    R.drawable.post_placeholder
                )
            }
        } else ivPostImg.remove()

        if (it.address.isNullOrEmpty()) tvAddress.remove()
        else {
            tvAddress.show()
            tvAddress.text = it.address
        }

        if (!it.startDate.isNullOrEmpty() && !it.endDate.isNullOrEmpty()) {
            val startDate = formatDateTime(it.startDate, "yyyy-MM-dd", "dd-yyyy-MMM")
            val endDate = formatDateTime(it.endDate, "yyyy-MM-dd", "dd-yyyy-MMM")
            val completeDate = getCombinedDateWithStartAndEndDate(startDate, endDate)
            tvExperience.text = completeDate
        } else {
            //jobDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tools, 0, 0, 0)
        }

        layoutSalary.show()
        tvSalary.text = getString(
            R.string.total_salary,
            it.compensation.toString()
        )

        if (!it.totalHours.isNullOrEmpty()) tvJobType.text =
            getString(
                R.string.total_hours,
                it.totalHours
            )
        else {
            layoutJobType.show()
            when (it.jobType) {
                1 -> tvJobType.text = getString(R.string.full_time)
                2 -> tvJobType.text = getString(R.string.part_time)
                3 -> tvJobType.text = getString(R.string.both)
            }
        }

        if (!layoutJobType.isVisible && !layoutExperience.isVisible) {
            cardJobDescription.show()
            tvJobDesc.text = it.description
        } else cardJobDescription.remove()
    }

    private fun handleDeleteJobRes() {
        jobsViewModel.deleteJobRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.apply {
                        if (JobsFragment.userType==2) jobsViewModel.individualJobList.removeAt(position!!)
                        else jobsViewModel.companyJobList.removeAt(position!!)
                        binding.progressBar.remove()
                        findNavController().navigateUp()
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), it.message.toString())
                }

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> binding.progressBar.remove()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jobsViewModel.clearEditPostJobRes()
    }
}