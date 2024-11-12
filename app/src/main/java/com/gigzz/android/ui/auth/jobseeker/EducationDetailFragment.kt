package com.gigzz.android.ui.auth.jobseeker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentEducationDetailBinding
import com.gigzz.android.databinding.FragmentProfileSetUpBinding
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.ui.home.HomeActivity
import com.gigzz.android.utils.EducationTime
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.restrictNameAndNumbersInput
import com.gigzz.android.utils.restrictOrganizationNameInput
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EducationDetailFragment : Fragment(R.layout.fragment_education_detail) {
    private val binding by viewBinding(FragmentEducationDetailBinding::bind)
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setClickListener()
    }

    private fun setClickListener() = binding.run {
        /* navigationImg.setOnClickListener {
             findNavController().popBackStack()
         }*/
        editStartYear.setOnClickListener {
            showDatePickerDialog(EducationTime.START)
        }
        editEndYear.setOnClickListener {
            showDatePickerDialog(EducationTime.END)
        }
        endYear.isEnabled = true
        editEndYear.isEnabled = true
        currentlyPursuing.setOnCheckedChangeListener { _, isChecked ->
            endYear.isEnabled = !isChecked
            editEndYear.isEnabled = !isChecked
            editEndYear.text = null
        }
        continueBtn.setOnClickListener {
            val eduModel = AddEditEducationReq(
                school = school.editText?.text.toString().trim(),
                grade = grade.editText?.text.toString().trim(),
                startYear = editStartYear.text.toString().trim(),
                endYear = editEndYear.text.toString().trim(),
                isPursuing = if (currentlyPursuing.isChecked) 1 else 0
            )
            //eduModel.educationId = model?.educationId
            authViewModel.editAddEducationApi(eduModel)
            handleAddEducationResponse()
        }
    }

    private fun showDatePickerDialog(educationTime: EducationTime) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this.requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedMonth + 1}-$selectedDay-$selectedYear"
                if (educationTime == EducationTime.END) binding.editEndYear.setText(selectedDate)
                else binding.editStartYear.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

   /* private fun openDatePicker(educationTime: EducationTime) {
        val dialogView =
            LayoutInflater.from(this.requireContext()).inflate(R.layout.year_picker_dialog, null)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)

        // Get the current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 1900
        yearPicker.maxValue = currentYear.plus(10)

        // Set the default value to the current year
        yearPicker.value = currentYear
        if (educationTime == EducationTime.END) {
            val startYear = binding.editStartYear.text.toString().toIntOrNull()
            if (startYear != null) {
                yearPicker.minValue = startYear + 1
            }
        }
        if (educationTime == EducationTime.START) yearPicker.maxValue = currentYear
        val dialog = AlertDialog.Builder(this.requireContext())
            .setTitle("Select a Year")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = yearPicker.value
                when (educationTime) {
                    EducationTime.START -> binding.editStartYear.setText(
                        selectedYear.toString()
                    )

                    EducationTime.END -> binding.editEndYear.setText(
                        selectedYear.toString()
                    )
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()
        dialog.show()
    }*/

    private fun initViews() = with(binding) {
        school.editText?.addTextChangedListener(textWatcher)
        grade.editText?.addTextChangedListener(textWatcher)
        editStartYear.addTextChangedListener(textWatcher)
        editEndYear.setOnItemClickListener { _, _, _, _ -> checkFieldsAndEnableButton() }
        currentlyPursuing.setOnCheckedChangeListener { _, _ -> checkFieldsAndEnableButton() }

    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            checkFieldsAndEnableButton()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    fun checkFieldsAndEnableButton() {
        val isSchoolFilled =
            binding.school.editText?.restrictOrganizationNameInput().toString().trim()
        val isGradeFilled = binding.grade.editText?.restrictNameAndNumbersInput().toString().trim()
        val isStartYearSelected = binding.editStartYear.text.toString().trim()
        val isCurrentlyPursuingChecked = binding.currentlyPursuing.isChecked
        binding.continueBtn.isEnabled =
            (isSchoolFilled.isNotEmpty() && isGradeFilled.isNotEmpty() && isStartYearSelected.isNotEmpty()) || isCurrentlyPursuingChecked
    }

    private fun handleAddEducationResponse() {
        authViewModel.verifyOtpResLiveData.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), res.message.toString())
                }

                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                is Resource.Success -> {
                    binding.progressBar.hide()
                    startActivity(Intent(requireActivity(), HomeActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}