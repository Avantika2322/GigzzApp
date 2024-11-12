package com.gigzz.android.ui.jobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentJobDetailsBinding
import com.gigzz.android.domain.res.JobData
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.formatDateTime
import com.gigzz.android.utils.getCombinedDateWithStartAndEndDate
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.parcelable
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast


class JobDetailsFragment : Fragment(R.layout.fragment_job_details) {
    private val binding by viewBinding(FragmentJobDetailsBinding::bind)
    private lateinit var jobData: JobsData
    private val viewModel: JobsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jobData = arguments?.parcelable<JobsData>("jobData")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.iv1.setImageResource(R.drawable.edit)
            toolbar.iv2.hide()
            toolbar.wishlist.show()
            //toolbar.iv2.setImageResource(R.drawable.ic_delete)
            toolbar.toolbarTitle.text=getString(R.string.job_details)
        }

        handleApplyJobRes()
        initViews()
        clickListeners()
    }

    private fun initViews() = with(binding){
        jobData.let {
            tvJobTitle.text= it.companyName
            if (it.images?.isNotEmpty() == true) {
                it.images[0].let { img ->
                    img.cachedImgThumbnailUrl = ivPostImg.loadCachedImg(
                        img.imageThumbnail,
                        img.cachedImgThumbnailUrl,
                        R.drawable.post_placeholder
                    )
                }
            }else ivPostImg.remove()

            if (it.jobType == 2) {
                if (it.companyAddress.isNullOrEmpty()) tvAddress.remove()
                else {
                    tvAddress.show()
                    tvAddress.text = it.companyAddress
                }
            } else tvAddress.text = it.address

            if (!it.startDate.isNullOrEmpty() && !it.endDate.isNullOrEmpty()) {
                val startDate = formatDateTime(it.startDate, "yyyy-MM-dd", "dd-yyyy-MMM")
                val endDate = formatDateTime(it.endDate, "yyyy-MM-dd", "dd-yyyy-MMM")
                val completeDate = getCombinedDateWithStartAndEndDate(startDate, endDate)
                tvExperience.text = completeDate
            } else {
                //jobDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tools, 0, 0, 0)
                if (it.experience.isNullOrEmpty()) layoutExperience.remove()
                else {
                    if (it.experience != "No") {
                        layoutExperience.show()
                        if (it.experience.isDigitsOnly() && it.experience.toDouble() < 1) tvExperience.text = getString(R.string.no_experience_required)
                        else tvExperience.text = getString(
                            R.string.total_experience,
                            it.experience.toString()
                        )
                    } else {
                        // jobDuration.gone()
                        tvExperience.text =
                            getString(R.string.no_experience)
                    }

                }
            }

            if (it.bookmarkStatus == 0) toolbar.wishlist.setImageResource(R.drawable.job_selected)
            else toolbar.wishlist.setImageResource(R.drawable.like)

            if (it.salary != null) {
                layoutSalary.show()
                tvSalary.text = getString(
                    R.string.total_salary,
                    it.salary.toString()
                )
            } else layoutSalary.remove()

            if (!it.totalHours.isNullOrEmpty()) tvJobType.text =
                getString(
                    R.string.total_hours,
                    it.totalHours
                )
            else if (it.totalHours.isNullOrEmpty() && it.jobTypes == null) {
                layoutJobType.remove()
            } else {
                layoutJobType.show()
                when (it.jobType) {
                    1 -> tvJobType.text = getString(R.string.full_time)
                    2 -> tvJobType.text = getString(R.string.part_time)
                    3 -> tvJobType.text = getString(R.string.both)
                }
            }

            if (!layoutJobType.isVisible && !layoutExperience.isVisible) {
                cardJobDescription.show()
                tvJobDesc.text = it.individualJobsDescription
            } else cardJobDescription.remove()

            if (!it.companyJobUrl.isNullOrEmpty() || !it.individualJobUrl.isNullOrEmpty()){
                ivApplyLink.show()
            }else ivApplyLink.remove()

            if (jobData.statusId== null){
                btnApply.show()
                layoutReview.hide()
                ivApplyLink.show()
                btnApply.text= getString(R.string.apply)
            }else{
                if (jobData.statusId == 2){
                    btnApply.hide()
                    ivApplyLink.hide()
                    layoutReview.show()
                }else{
                    layoutReview.hide()
                    btnApply.show()
                    ivApplyLink.hide()
                    btnApply.text= getString(R.string.applied)
                }
            }
        }
    }

    private fun clickListeners() = binding.run{
        toolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        toolbar.wishlist.setOnClickListener {
            if (jobData.bookmarkStatus == 0) {
                toolbar.wishlist.setImageResource(R.drawable.like)
                viewModel.bookmarkJobApi(jobData.jobId)
            }
            else {
                toolbar.wishlist.setImageResource(R.drawable.job_selected)
                viewModel.removeBookmarkJobApi(jobData.jobId, "allJobs")
            }
        }

        btnApply.setOnClickListener {
            if (!jobData.companyJobUrl.isNullOrEmpty() || !jobData.individualJobUrl.isNullOrEmpty()){
                //
            }else {
                viewModel.applyJobApi(jobData.jobId)
            }
        }
    }

    private fun handleApplyJobRes() {
        viewModel.applyJobRes.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Error -> binding.progressBar.hide()
                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(),getString(R.string.no_internet))
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    binding.btnApply.text= getString(R.string.applied)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearApplyJobRes()
    }
}