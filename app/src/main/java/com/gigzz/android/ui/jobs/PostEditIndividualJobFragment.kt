package com.gigzz.android.ui.jobs

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentPostEditIndividualJobBinding
import com.gigzz.android.domain.req.PostNewJobByIndividualReq
import com.gigzz.android.domain.res.JobDataById
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.utils.JobDate
import com.gigzz.android.utils.JobTime
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.formatDateTime
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.hideKeyboard
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Boolean.getBoolean
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

@AndroidEntryPoint
class PostEditIndividualJobFragment : Fragment(R.layout.fragment_post_edit_individual_job) {
    private val binding by viewBinding(FragmentPostEditIndividualJobBinding::bind)
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var categoryName: String = ""
    private var categoryList = ArrayList<String>()
    private var selectedStartDateInMillis: Long = 0
    private var selectedEndDateInMillis: Long = 0
    private var isEdit = false
    private var currentJobId: Int? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getMasterDataApi()
        arguments?.apply {
            isEdit= getBoolean("isFromEdit")
            currentJobId= getInt("jobId")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
        textWatchers()
        initViews()
        handleMasterDataRes()
        if (isEdit) handleJobDetailRes()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() = with(binding) {
        etJobDescLayout.editText!!.setOnTouchListener { view, motionEvent ->
            svPostJob.requestDisallowInterceptTouchEvent(true)
            false
        }

        etSalary.filters =
            arrayOf(
                DecimalDigitsInputFilter(
                    4,
                    1
                )
            )

    }

    private fun setClickListener() = binding.run {
        ivBack.setOnClickListener { findNavController().popBackStack() }
        (etJobStartTimeLayout.editText as? AutoCompleteTextView)?.setOnClickListener {
            openTimePicker(JobTime.START)
        }
        (etJobEndTimeLayout.editText as? AutoCompleteTextView)?.setOnClickListener {
            openTimePicker(JobTime.END)
        }
        (etJobStartDayLayout.editText as? AutoCompleteTextView)?.setOnClickListener {
            openDatePicker(JobDate.START)
        }
        (etJobEndDayLayout.editText as? AutoCompleteTextView)?.setOnClickListener {
            openDatePicker(JobDate.END)
        }

        postJobBtn.setOnClickListener {
            if (validation()) {
                val compensation = if (etSalary.text.toString().trim()
                        .isNotEmpty() && etSalary.text.toString().trim().contains(".")
                ) {
                    when (etSalary.text.toString().trim().substringAfter(".").length) {
                        2 -> etSalary.text.toString().trim()
                        else -> etSalary.text.toString().trim() + "0"
                    }
                } else etSalary.text.toString().trim() + ".00"

                if (isEdit){
                    val model = PostNewJobByIndividualReq(
                        category_name = categoryName,
                        compensation = compensation,
                        description = etJobDesc.text.toString().trim(),
                        end_date = etJobEndDayLayout.editText?.text.toString().trim(),
                        start_date = etJobStartDayLayout.editText?.text.toString().trim(),
                        start_time = etStartTime.text.toString().trim(),
                        end_time = etEndTime.text.toString().trim(),
                        is_parent_supervision_required = if (teensCheckbox.isChecked) 1 else 0,
                        job_name = etJobName.text.toString().trim(),
                        total_hours = etJobHours.text.toString().trim(),
                        individual_job_url = etWebUrl.text.toString().trim(),
                        job_location = if(radioGroupLocation.checkedRadioButtonId== R.id.radioOnsite) 1 else 0,
                        job_id = currentJobId
                    )
                    jobsViewModel.editJobAsIndividual(model)
                    handlePostJobByIndividualsResponse()
                }else{
                    val model = PostNewJobByIndividualReq(
                        category_name = categoryName,
                        compensation = compensation,
                        description = etJobDesc.text.toString().trim(),
                        end_date = etJobEndDayLayout.editText?.text.toString().trim(),
                        start_date = etJobStartDayLayout.editText?.text.toString().trim(),
                        start_time = etStartTime.text.toString().trim(),
                        end_time = etEndTime.text.toString().trim(),
                        is_parent_supervision_required = if (teensCheckbox.isChecked) 1 else 0,
                        job_name = etJobName.text.toString().trim(),
                        total_hours = etJobHours.text.toString().trim(),
                        individual_job_url = etWebUrl.text.toString().trim(),
                        job_location = if(radioGroupLocation.checkedRadioButtonId== R.id.radioOnsite) 1 else 0
                    )
                    jobsViewModel.postNewJobByIndividual(model)
                    handlePostJobByIndividualsResponse()
                }
            }
        }
    }

    private fun handlePostJobByIndividualsResponse() {
        jobsViewModel.postEditJobRes.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Loading-> binding.progressBar.show()
                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), it.message.toString())
                }
                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    jobsViewModel.clearEditPostJobRes()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun validation(): Boolean {
        with(binding) {
            val jobTitle = etJobName.text.toString().trim()
            val startDate = etJobStartDayLayout.editText?.text.toString().trim()
            val endDate = etJobEndDayLayout.editText?.text.toString().trim()
            val hours = etJobHours.text.toString().trim()
            val compensation = etSalary.text.toString().trim()
            val description = etJobDesc.text.toString().trim()
            when {
                jobTitle.isEmpty() -> {
                    etJobName.error = "Please enter job name!"
                    return false
                }

                startDate.isEmpty() && endDate.isNotEmpty() -> {
                    etJobStartDayLayout.editText?.error = "Please enter start date!"
                    return false
                }

                startDate.isNotEmpty() && endDate.isEmpty() -> {
                    etJobEndDayLayout.editText?.error = "Please enter end date!"
                    return false
                }

                endDate < startDate -> {
                    etJobEndDayLayout.editText?.error = "Please select a valid end date!"
                    return false
                }

                hours== "0"-> {
                    etJobHours.error = "Total hours can not be zero!"
                    return false
                }

                compensation== "0" -> {
                    etSalary.error = "Compensation should not be zero!"
                    return false
                }

                compensation.isNotEmpty() && compensation.toDouble() > 10000F -> {
                    etSalary.error = "Compensation should be less or equal to 10000!"
                    return false
                }

                description.isEmpty() -> {
                    etJobDesc.error = "Please enter description!"
                    return false
                }

                else -> return true
            }
        }
    }

    private fun textWatchers() = binding.run {
        etJobName.addTextChangedListener(jobName)
        etStartTime.addTextChangedListener(startTime)
        etJobHours.addTextChangedListener(hours)
        etSalary.addTextChangedListener(compensation)
        etJobDesc.addTextChangedListener(description)
    }

    private val jobName = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etJobName.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etJobName.error = "Please enter job name!"
            } else {
                binding.etJobName.error = null
            }
        }
    }

    private val hours = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etJobHours.error = null
            updateEndTime()
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isNotEmpty() == true && p0.substring(0, 1).toInt() == 0) {
                binding.etJobHours.error = resources.getString(R.string.valid_hours)
            } else {
                binding.etJobHours.error = null
            }
        }
    }


    private val startTime = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etJobStartTimeLayout.editText?.error = null
            updateEndTime()
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etJobStartTimeLayout.editText?.error = "Please enter start time!"
            } else {
                binding.etJobStartTimeLayout.editText?.error = null
            }
        }
    }


    private val compensation = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etSalary.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etSalary.error = resources.getString(R.string.enter_compensation)
            } else if (p0?.length!! >= 2 && p0.substring(0, 2) == ".0") {
                binding.etSalary.error =
                    resources.getString(R.string.less_zero_compensation)
            } else if (p0.substring(0, 1) == ".") {
                // binding.editJobCompensation.error = resources.getString(R.string.less_zero_compensation)
            } else if (p0.length >= 3 && p0.substring(0, 3) == "0.0") {
                binding.etSalary.error =
                    resources.getString(R.string.less_zero_compensation)
            } else if (p0.isNotEmpty() && p0.substring(0, 1).toInt() == 0) {
                binding.etSalary.error = resources.getString(R.string.valid_compensation)
            } else if (p0.isNotEmpty() && p0.toString().toDouble() > 10000F) {
                binding.etSalary.error =
                    resources.getString(R.string.enter_valid_compensation)
            } else {
                binding.etSalary.error = null
            }
        }
    }


    private val description = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etJobDesc.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etJobDesc.error = "Please enter description!"
            } else {
                binding.etJobDesc.error = null
            }
        }
    }

    private fun openDatePicker(jobDate: JobDate) {
        val constraintsBuilder = CalendarConstraints.Builder()
        val now = DateValidatorPointForward.now()
        constraintsBuilder.setValidator(now)

        when (jobDate) {
            JobDate.START -> {
                if (selectedEndDateInMillis.toInt() != 0) {
                    val minDate = MaterialDatePicker.todayInUtcMilliseconds()
                    constraintsBuilder.setOpenAt(minDate)
                }
            }

            JobDate.END -> {
                if (selectedStartDateInMillis.toInt() != 0) {
                    val minDate = selectedStartDateInMillis + 24 * 60 * 60 * 1000
                    val dateRange = DateValidatorPointForward.from(minDate)
                    constraintsBuilder.setValidator(dateRange)
                    constraintsBuilder.setOpenAt(minDate)
                }
            }
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(jobDate.title))
            .setCalendarConstraints(constraintsBuilder.build())
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            val localDate =
                Instant.ofEpochMilli(selectedDateInMillis).atZone(ZoneId.systemDefault())
                    .toLocalDate()
            val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
            val date = localDate.format(formatter)

            when (jobDate) {
                JobDate.START -> {
                    (binding.etJobStartDayLayout.editText as? AutoCompleteTextView)?.setText(date)
                    selectedStartDateInMillis = selectedDateInMillis
                }

                JobDate.END -> {
                    (binding.etJobEndDayLayout.editText as? AutoCompleteTextView)?.setText(date)
                    selectedEndDateInMillis = selectedDateInMillis
                }
            }
        }

        datePicker.show(parentFragmentManager, "datePicker")
    }

    private fun updateEndTime() {
        val startTimeText = binding.etStartTime.text.toString()
        val hoursText = binding.etJobHours.text.toString()

        if (startTimeText.isNotEmpty() && hoursText.isNotEmpty()) {
            val isSystem24Hour = DateFormat.is24HourFormat(requireContext())
            val calendar = Calendar.getInstance()
            val startTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(startTimeText)
            calendar.time = startTime!!
            calendar.add(Calendar.HOUR_OF_DAY, hoursText.trim().toInt())

            val endTimeAmPm = if (calendar.get(Calendar.HOUR_OF_DAY) < 12) "AM" else "PM"
            val endFormattedHour =
                if (isSystem24Hour) calendar.get(Calendar.HOUR_OF_DAY) else calendar.get(Calendar.HOUR_OF_DAY) % 12
            val endTimeText = String.format(
                "%02d:%02d %s",
                endFormattedHour,
                calendar.get(Calendar.MINUTE),
                endTimeAmPm
            )
            (binding.etJobEndTimeLayout.editText as? AutoCompleteTextView)?.setText(endTimeText)
        }
    }

    private fun openTimePicker(jobTime: JobTime) {
        val isSystem24Hour = DateFormat.is24HourFormat(requireContext())
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val titleText = if (jobTime == JobTime.START) getString(R.string.job_start_time_msg)
        else getString(R.string.job_end_time_msg)

        val timePicker = MaterialTimePicker.Builder().setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
            .setTimeFormat(clockFormat)
            .setTitleText(titleText)
            .build()
        timePicker.show(childFragmentManager, "TimePicker")
        timePicker.addOnPositiveButtonClickListener {
            val isAM = timePicker.hour < 12
            val amPm = if (isAM) "AM" else "PM"
            val formattedHour = if (isSystem24Hour) timePicker.hour else timePicker.hour % 12
            val minutes = if (timePicker.minute < 10) "0${timePicker.minute}" else timePicker.minute
            val selectedTime = "${formattedHour}:$minutes $amPm"
            when (jobTime) {
                JobTime.START ->
                    (binding.etJobStartTimeLayout.editText as? AutoCompleteTextView)?.setText(
                        selectedTime
                    )

                JobTime.END -> (binding.etJobEndTimeLayout.editText as? AutoCompleteTextView)?.setText(
                    selectedTime
                )
            }
        }
    }

    private fun handleMasterDataRes() {
        profileViewModel.getMasterRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    categoryList.clear()
                    it.data?.data?.let { it ->
                        binding.apply {
                            it.let {
                                if (!it.category.isNullOrEmpty()) {
                                    for (i in 0..<it.category.size) {
                                        /*if (it.category[i].name == args.jobDetails?.categoryName) {
                                            binding.etCategory.setText(res.data.category[i].name)
                                            categoryName = res.data.category[i].name
                                        }*/
                                        if (it.category[i].category_job_type == 1) categoryList.add(
                                            it.category[i].name
                                        )
                                    }
                                    val adapter: ArrayAdapter<String> =
                                        ArrayAdapter<String>(
                                            requireActivity(),
                                            android.R.layout.simple_dropdown_item_1line,
                                            categoryList
                                        )
                                    etCategory.threshold = 3
                                    etCategory.setAdapter(adapter)
                                    etJobCategoryLayout.setEndIconOnClickListener {
                                        hideKeyboard(binding.etCategory)
                                    }
                                    etCategory.setOnItemClickListener { adapterView, view, i, l ->
                                        if (adapterView.getItemAtPosition(i)
                                                .toString() == it.category[i].name
                                        ) {
                                            categoryName = if (it.category[i].name == "Other") {
                                                etJobCategoryLayout.show()
                                                etOthersLayout.editText?.text.toString().trim()
                                            } else {
                                                etJobCategoryLayout.remove()
                                                it.category[i].name
                                            }
                                        }
                                    }
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

    private fun handleJobDetailRes() {
        jobsViewModel.getPostedJobByIdRes.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.apply {
                        binding.progressBar.remove()
                        it.data?.data?.let { data ->
                            binding.apply {
                                setDataOnUi(data)
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

    private fun setDataOnUi(data: JobDataById) = binding.apply {
        data.apply {
            postJobSubHead.text = resources.getString(R.string.edit_detail_subtitle)
            postJobBtn.text = resources.getString(R.string.save_changes)
            etJobDesc.setText(data.description)
            if (!categoryName.isNullOrEmpty()) etCategory.setText(categoryName)
            if (!endDate.isNullOrEmpty()) etJobEndDayLayout.editText?.setText(
                formatDateTime(
                    endDate,
                    "yyyy-MM-dd",
                    "MM-dd-yyyy"
                )
            )
            if (!startDate.isNullOrEmpty()) etJobStartDayLayout.editText?.setText(
                formatDateTime(
                    startDate,
                    "yyyy-MM-dd",
                    "MM-dd-yyyy"
                )
            )
            if (!startTime.isNullOrEmpty()) etStartTime.setText(startTime)
            if (!endTime.isNullOrEmpty()) etEndTime.setText(endTime)
            etJobName.setText(jobName)
            if (!totalHours.isNullOrEmpty()) etJobHours.setText(
                getString(
                    R.string.total_hours,
                    totalHours.toString()
                )
            )
            teensCheckbox.isChecked = true
            if (!compensation.isNullOrEmpty()) compensation.let {
                etSalary.setText(
                    compensation.toString()
                )
            }
            if (!individualJobUrl.isNullOrEmpty()) etWebUrl.setText(individualJobUrl.toString())
        }

    }
}


class DecimalDigitsInputFilter(digitsBeforeDecimal: Int, digitsAfterDecimal: Int) :
    InputFilter {
    private val pattern: Pattern =
        Pattern.compile("[0-9]{0,$digitsBeforeDecimal}+((\\.[0-9]{0,$digitsAfterDecimal})?)||(\\.)?")

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        val matcher = pattern.matcher(dest)
        return if (!matcher.matches()) "" else null
    }
}