package com.gigzz.android.ui.jobs

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentJobFilterBottomBinding
import com.gigzz.android.domain.req.AllJobsFilterReq
import com.gigzz.android.domain.req.CompanyJobsFilterReq
import com.gigzz.android.domain.req.IndividualJobsFilterReq
import com.gigzz.android.domain.res.FilterData
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hideKeyboard
import com.gigzz.android.utils.isNetworkAvailable
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.trimSubstring
import kotlin.math.min

@AndroidEntryPoint
class JobFilterBottomFragment(val type: String) :
    BottomSheetDialogFragment(R.layout.fragment_job_filter_bottom) {
    private val binding by viewBinding(FragmentJobFilterBottomBinding::bind)
    private val viewModel: JobsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var gigzzTypeAdapter: GigzzTypeAdapter
    private var fieldOfWorkData = ArrayList<FilterData>()
    private var jobCategoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getMasterDataApi()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (type) {
            "individual" -> initIndividualView()
            "company" -> initCompanyView()
            else -> initJobSeekerView()
        }
        clickListeners()
        handleMasterDataRes()
    }

    private fun initJobSeekerView() = with(binding) {
        if (type == "applied") {
            radioPostedTime.remove()
            tvRadius.remove()
            radiusMinRange.remove()
            radiusMaxRange.remove()
            radiusSliderRange.remove()
            tvJobStatus.remove()
            jobStatusChip.remove()
            tvJobAge.remove()
            radioGrpAge.remove()
            tvPayment.remove()
            maxRange.remove()
            minRange.remove()
            sliderRange.remove()
            tvField.remove()
            rvFieldRecycler.remove()
        } else {
            //jobType,radius,posted date time
            tvPayment.remove()
            maxRange.remove()
            minRange.remove()
            sliderRange.remove()
            tvField.remove()
            rvFieldRecycler.remove()
            tvJobStatus.remove()
            jobStatusChip.remove()
            radioGrpAge.remove()
            tvJobAge.text = getString(R.string.posted_time)

            if (viewModel.filterAllJobsFilterReq.jobType != -1) {
                when (viewModel.filterAllJobsFilterReq.jobType) {
                    0 -> {
                        tvFullTime.isChecked = true
                        tvFullTime.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    1 -> {
                        tvPartTime.isChecked = true
                        tvPartTime.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    2 -> {
                        tvBoth.isChecked = true
                        tvBoth.setTextColor(getColor(requireContext(), R.color.white))
                    }
                }
            }
        }
    }

    private fun initCompanyView() = with(binding) {
        radioPostedTime.remove()
        tvRadius.remove()
        radiusMinRange.remove()
        radiusMaxRange.remove()
        radiusSliderRange.remove()
        setAdapter()
        maxRange.text =
            getString(R.string.slider_payment_dash, 1000)
        minRange.text = getString(R.string.slider_payment, 0)
        viewModel.filterCompanyJobsReq.let {
            if (it.jobStatus != null) {
                when (it.jobStatus) {
                    0 -> {
                        tvAll.isChecked = true
                        tvAll.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    1 -> {
                        tvOpen.isChecked = true
                        tvOpen.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    2 -> {
                        tvCompleted.isChecked = true
                        tvCompleted.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    3 -> {
                        tvInProgress.isChecked = true
                        tvInProgress.setTextColor(getColor(requireContext(), R.color.white))
                    }
                }

                when (it.jobTypes?.get(0)) {
                    0 -> {
                        tvFullTime.isChecked = true
                        tvFullTime.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    1 -> {
                        tvPartTime.isChecked = true
                        tvPartTime.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    2 -> {
                        tvBoth.isChecked = true
                        tvBoth.setTextColor(getColor(requireContext(), R.color.white))
                    }
                }

                if (it.ageRequirement == 0) underAge.isChecked =
                    true else if (it.ageRequirement == 2) overAge.isChecked = true

                if (it.minCompensation != null) {
                    sliderRange.values =
                        listOf(
                            it.minCompensation!!.toFloat(),
                            it.maxCompensation!!.toFloat()
                        )
                    maxRange.text =
                        getString(R.string.slider_payment_dash, it.maxCompensation)
                    minRange.text = getString(R.string.slider_payment, it.minCompensation)
                } else sliderRange.values = listOf(0F, 10000F)
            }
        }

    }

    private fun initIndividualView() = with(binding) {
        radioPostedTime.remove()
        tvRadius.remove()
        radiusMinRange.remove()
        radiusMaxRange.remove()
        radiusSliderRange.remove()
        tvJobType.remove()
        jobTypeChip.remove()
        tvJobAge.remove()
        radioGrpAge.remove()
        tvPayment.text = getString(R.string.payment)
        setAdapter()
        maxRange.text =
            getString(R.string.slider_payment_dash, 1000)
        minRange.text = getString(R.string.slider_payment, 0)
        viewModel.filterIndividualJobsReq.let {
            if (it.jobStatus != null) {
                when (it.jobStatus) {
                    0 -> {
                        tvAll.isChecked = true
                        tvAll.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    1 -> {
                        tvOpen.isChecked = true
                        tvOpen.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    2 -> {
                        tvCompleted.isChecked = true
                        tvCompleted.setTextColor(getColor(requireContext(), R.color.white))
                    }

                    3 -> {
                        tvInProgress.isChecked = true
                        tvInProgress.setTextColor(getColor(requireContext(), R.color.white))
                    }
                }

                sliderRange.values =
                    listOf(
                        it.minCompensation!!.toFloat(),
                        it.maxCompensation!!.toFloat()
                    )
                maxRange.text =
                    getString(R.string.slider_payment_dash, it.maxCompensation)
                minRange.text = getString(R.string.slider_payment, it.minCompensation)
            }
        }
    }

    private fun setAdapter() {
        val flexboxLayoutManager = FlexboxLayoutManager(requireActivity())
        flexboxLayoutManager.apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        binding.rvFieldRecycler.layoutManager = flexboxLayoutManager
        gigzzTypeAdapter =
            GigzzTypeAdapter(
                fieldOfWorkData,
                viewModel.selectedGigzz
            ) { pos, model, src ->
                model.isSelected = !model.isSelected
                // categoryList.remove(model.categoryName)
                if (model.isSelected) {
                    viewModel.selectedGigzz = model.categoryName
                    jobCategoryName = model.categoryName
                    //for creating list of selected categoryList
                    //  categoryList.add(model.categoryName)
                }
                gigzzTypeAdapter.notifyDataSetChanged()
            }
        binding.rvFieldRecycler.adapter = gigzzTypeAdapter
    }

    private fun clickListeners() = binding.run {
        applyFilterBtn.setOnClickListener {
            val currentJobStatus = if (tvFullTime.isChecked) 0
            else if (tvPartTime.isChecked) 1
            else 2

            when (type) {
                "all" -> {
                    if (tvFullTime.isChecked || tvPartTime.isChecked || tvBoth.isChecked) {
                        val dateTime = if (rb24Hours.isChecked) 1
                        else if (rb3Days.isChecked) 2
                        else if (rb15days.isChecked) 3
                        else if (rbLastWeek.isChecked) 4 else null
                        viewModel.filterAllJobsFilterReq.apply {
                            jobType = currentJobStatus
                            minRadius =
                                radiusMinRange.text.toString().substringBefore("Miles").trim()
                                    .toInt()
                            maxRadius = radiusMaxRange.text.toString().substringBefore("Miles")
                                .trimSubstring(1).trim().toInt()
                            postedTime = dateTime!!
                            pageNo = 1
                        }
                        if (isNetworkAvailable(requireContext())) viewModel.filterAllJobs()
                        else showToast(requireContext(), getString(R.string.no_internet))
                        dismiss()
                    } else showToast(requireContext(), "Please select all the required field!")
                }

                "applied" -> {
                    viewModel.selectedJobTypeApplied = currentJobStatus
                    if (tvFullTime.isChecked || tvPartTime.isChecked || tvBoth.isChecked) {
                        if (isNetworkAvailable(requireContext())) viewModel.filterAppliedJobs(
                            status = currentJobStatus
                        )
                        else showToast(requireContext(), getString(R.string.no_internet))
                        dismiss()
                    } else showToast(requireContext(), "Please select all the required field! ")
                }

                "individual" -> {
                    viewModel.selectedGigzz = jobCategoryName
                    if (tvAll.isChecked || tvInProgress.isChecked || tvCompleted.isChecked || tvOpen.isChecked) {
                        viewModel.filterIndividualJobsReq.apply {
                            categoryName1 = jobCategoryName
                            jobStatus = currentJobStatus
                            minCompensation =
                                minRange.text.toString().trim().substringAfter("$").toInt()
                            maxCompensation =
                                maxRange.text.toString().trim().substringAfter("$").trimSubstring(0)
                                    .toInt()
                        }
                        if (isNetworkAvailable(requireContext())) viewModel.filterIndividualJobs()
                        else showToast(requireContext(), getString(R.string.no_internet))
                        dismiss()
                    } else showToast(requireContext(), "Please select all the required field! ")
                }

                "company" -> {
                    val jobType = if (tvFullTime.isChecked) 0
                    else if (tvPartTime.isChecked) 1
                    else if (tvBoth.isChecked) 2 else null
                    viewModel.selectedGigzz = jobCategoryName
                    if (tvAll.isChecked || tvOpen.isChecked || tvCompleted.isChecked || tvInProgress.isChecked) {
                        (if (underAge.isChecked) 0 else if (midAge.isChecked) 1 else if (overAge.isChecked) 2 else null)?.let { it1 ->
                            viewModel.filterCompanyJobsReq.apply {
                                jobStatus = currentJobStatus
                                minCompensation = minRange.text.toString().trim()
                                    .substringAfter("$")
                                    .toInt()
                                maxCompensation = maxRange.text.toString().trim()
                                    .substringAfter("$")
                                    .trimSubstring(0)
                                    .toInt()
                                categoryName = jobCategoryName
                                jobTypes = arrayListOf(jobType!!)
                                ageRequirement = it1
                                pageNo = 1
                            }
                        }
                        if (isNetworkAvailable(requireContext())) viewModel.filterCompanyJobs()
                        else showToast(requireContext(), getString(R.string.no_internet))
                        dismiss()
                    } else showToast(requireContext(), "Please select all the required field! ")
                }
            }
        }

        resetBtn.setOnClickListener {
            jobCategoryName=""
            when (type) {
                "all" -> viewModel.getAllJobs(1, JobsFragment.jobType!!)
                "applied" -> {
                    viewModel.getMyAppliedJobs()
                    viewModel.selectedGigzz=""
                    viewModel.filterAllJobsFilterReq = AllJobsFilterReq()
                }
                "company" -> {
                    viewModel.getAllCompanyJobs()
                    viewModel.selectedGigzz=""
                    viewModel.filterCompanyJobsReq = CompanyJobsFilterReq()
                }
                "individual"-> {
                    viewModel.getAllIndividualJobs()
                    viewModel.selectedGigzz=""
                    viewModel.filterIndividualJobsReq= IndividualJobsFilterReq()
                }
            }
            dismiss()
        }

        jobStatusChip.setOnCheckedStateChangeListener { group, checkedIds ->
            when(checkedIds[0]){
                R.id.tvAll -> {
                    tvAll.setTextColor(getColor(requireContext(), R.color.white))
                    tvOpen.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvCompleted.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvInProgress.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                }
                R.id.tvOpen -> {
                    tvOpen.setTextColor(getColor(requireContext(), R.color.white))
                    tvAll.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvCompleted.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvInProgress.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                }
                R.id.tvCompleted -> {
                    tvCompleted.setTextColor(getColor(requireContext(), R.color.white))
                    tvAll.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvOpen.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvInProgress.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                }
                R.id.tvInProgress -> {
                    tvInProgress.setTextColor(getColor(requireContext(), R.color.white))
                    tvAll.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvOpen.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvCompleted.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                }
            }
        }

        jobTypeChip.setOnCheckedStateChangeListener { group, checkedIds ->
            when(checkedIds[0]){
                R.id.tvFullTime -> {
                    tvFullTime.setTextColor(getColor(requireContext(), R.color.white))
                    tvPartTime.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvBoth.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                }
                R.id.tvPartTime -> {
                    tvPartTime.setTextColor(getColor(requireContext(), R.color.white))
                    tvFullTime.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvBoth.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                }
                R.id.tvBoth -> {
                    tvBoth.setTextColor(getColor(requireContext(), R.color.white))
                    tvFullTime.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                    tvPartTime.setTextColor(getColor(requireContext(), R.color.dark_grey_shade_1))
                }
            }
        }
    }

    private fun handleMasterDataRes() {
        profileViewModel.getMasterRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    fieldOfWorkData.clear()
                    it.data?.data?.let { it ->
                        binding.apply {
                            it.let {
                                if (!it.category.isNullOrEmpty()) {
                                    for (i in 0..<it.category.size) {
                                        if (it.category[i].category_job_type == JobsFragment.userType?.minus(
                                                1
                                            )
                                        ) {
                                            fieldOfWorkData.add(
                                                FilterData(
                                                    it.category[i].name,
                                                    false,
                                                    it.category[i].category_id
                                                )
                                            )
                                        }
                                    }
                                    gigzzTypeAdapter.notifyDataSetChanged()
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
}