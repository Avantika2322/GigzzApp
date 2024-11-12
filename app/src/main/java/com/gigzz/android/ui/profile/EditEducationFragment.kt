package com.gigzz.android.ui.profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.NumberPicker
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentEditEducationBinding
import com.gigzz.android.databinding.FragmentEditProfileBinding
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.domain.res.EducationDetailsData
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.utils.EducationTime
import com.gigzz.android.utils.JobDate
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.getYearOnly
import com.gigzz.android.utils.parcelable
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import java.util.Calendar


class EditEducationFragment : Fragment(R.layout.fragment_edit_education) {
    private val binding by viewBinding(FragmentEditEducationBinding::bind)
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)
    var model: EducationDetailsData? = null
    private var edit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            model = it.parcelable("model")
            edit = it.getBoolean("edit", false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        textWatchers()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        (editJobStartDay.editText as? AutoCompleteTextView)?.setOnClickListener {
            showDatePickerDialog(JobDate.START)
        }
        (editEndYear.editText as? AutoCompleteTextView)?.setOnClickListener {
            showDatePickerDialog(JobDate.END)
        }

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        saveBtn.setOnClickListener {
            if (validation()) {
                val eduModel = AddEditEducationReq(
                    school = etSchool.text.toString().trim(),
                    grade = etGrade.text.toString().trim(),
                    startYear = etStartDate.text.toString().trim(),
                    endYear = etEndYear.text.toString().trim(),
                    isPursuing = if (currentlyPursuingCheckbox.isChecked) 1 else 0
                )
                if (edit) {
                    eduModel.educationId = model?.educationId
                    profileViewModel.editUserEducationApi(eduModel)
                    handleEditEducationResponse()
                } else {
                    profileViewModel.addUserEducationApi(eduModel)
                    handleAddEducationResponse()
                }
            }
        }
    }


    private fun initViews() = with(binding) {
        if (edit) {
            etSchool.setText(model?.school)
            etGrade.setText(model?.grade)
            etStartDate.setText(getYearOnly(model?.startYear!!))
            if (getYearOnly(model?.endYear!!) == "0000") {
                editEndYear.isEnabled = false
                editEndYear.setEndIconTintList(ColorStateList.valueOf(Color.parseColor("#A1A1A1")))
            } else etEndYear.setText(getYearOnly(model?.endYear!!))
            if (model?.isPursuing == 1) currentlyPursuingCheckbox.isChecked = true
        } else {
            postJobHead.text = getString(R.string.add_a_new_education)
            postJobSubHead.text = getString(R.string.your_education_details)
            saveBtn.text = getString(R.string.add)
        }
        currentlyPursuingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            etEndYear.isEnabled = !isChecked
            etEndYear.setText("")
            editEndYear.isEnabled = !isChecked
            editEndYear.setEndIconTintList(ColorStateList.valueOf(Color.parseColor("#A1A1A1")))
        }

    }

    private fun validation(): Boolean {
        with(binding) {
            val school = etSchool.text.toString().trim()
            val grade = etGrade.text.toString().trim()
            val startYear = etStartDate.text.toString().trim()
            val endDate = etEndYear.text.toString().trim()

            when {
                school.isEmpty() -> {
                    editSchool.error = resources.getString(R.string.enter_school_name)
                    return false
                }

                grade.isEmpty() -> {
                    editGrade.error = resources.getString(R.string.enter_grade)
                    return false
                }

                startYear.isEmpty() -> {
                    etStartDate.error = resources.getString(R.string.enter_start_year)
                    return false
                }

                endDate.isEmpty() -> {
                    if (!currentlyPursuingCheckbox.isChecked) {
                        editEndYear.error = resources.getString(R.string.enter_end_year)
                        return false
                    } else {
                        editEndYear.error = ""
                        return true
                    }
                }

                else -> return true

            }
        }
    }

    private fun textWatchers() = binding.run {
        etSchool.addTextChangedListener(school)
        etGrade.addTextChangedListener(grade)
        etStartDate.addTextChangedListener(startDate)
        etEndYear.addTextChangedListener(endDate)

    }

    private val school = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.editSchool.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.editSchool.error = resources.getString(R.string.enter_school_name)
            } else {
                binding.editSchool.error = null
            }
        }
    }
    private val grade = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.editGrade.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.editGrade.error = resources.getString(R.string.enter_grade)
            } else {
                binding.editGrade.error = null
            }
        }
    }

    private val startDate = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.editJobStartDay.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.editJobStartDay.error = resources.getString(R.string.enter_start_year)
            } else {
                binding.editJobStartDay.error = null
            }
        }
    }
    private val endDate = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.editEndYear.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                if (!binding.currentlyPursuingCheckbox.isChecked) {
                    binding.editEndYear.error = resources.getString(R.string.enter_end_year)
                }
            } else {
                binding.editEndYear.error = null
            }
        }
    }

    private fun showDatePickerDialog(jobDate: JobDate) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this.requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedMonth + 1}-$selectedDay-$selectedYear"
                when (jobDate) {
                    JobDate.START -> (binding.editJobStartDay.editText as? AutoCompleteTextView)?.setText(
                        selectedDate
                    )

                    JobDate.END -> (binding.editEndYear.editText as? AutoCompleteTextView)?.setText(
                        selectedDate
                    )
                }
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun openDatePicker(jobDate: JobDate) {
        val dialogView =
            LayoutInflater.from(this.requireContext()).inflate(R.layout.year_picker_dialog, null)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)

        // Get the current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2035

        // Set the default value to the current year
        yearPicker.value = currentYear
        if (jobDate == JobDate.END) {
            val startYear =
                (binding.editJobStartDay.editText as? AutoCompleteTextView)?.text?.toString()
                    ?.toIntOrNull()
            if (startYear != null) {
                yearPicker.minValue = startYear + 1
            }
        }
        if (jobDate == JobDate.START) yearPicker.maxValue = 2023

        val dialog = AlertDialog.Builder(this.requireContext())
            .setTitle("Select a Year")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = yearPicker.value
                when (jobDate) {
                    JobDate.START -> (binding.editJobStartDay.editText as? AutoCompleteTextView)?.setText(
                        selectedYear.toString()
                    )

                    JobDate.END -> (binding.editEndYear.editText as? AutoCompleteTextView)?.setText(
                        selectedYear.toString()
                    )
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()
        dialog.show()
    }

    private fun handleAddEducationResponse() {
        profileViewModel.addEducationRes.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success->{
                    binding.progressBar.remove()
                    findNavController().popBackStack()
                }

                is Resource.Error -> binding.progressBar.remove()
                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(),getString(R.string.no_internet))
                }
                is Resource.Loading ->  binding.progressBar.show()
            }
        }
    }

    private fun handleEditEducationResponse() {
        profileViewModel.editEducationRes.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success->{
                    binding.progressBar.remove()
                    findNavController().popBackStack()
                }

                is Resource.Error -> binding.progressBar.remove()
                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(),getString(R.string.no_internet))
                }
                is Resource.Loading ->  binding.progressBar.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        profileViewModel.clearEditAddEduRes()
    }
}