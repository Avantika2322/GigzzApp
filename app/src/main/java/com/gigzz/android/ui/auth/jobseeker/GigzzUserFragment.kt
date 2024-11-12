package com.gigzz.android.ui.auth.jobseeker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentGigzzUserBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.isEmailValid
import com.gigzz.android.utils.isPhoneNumberValid
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.restrictNameInput
import com.gigzz.android.utils.setupSpaceAndLengthRestriction
import com.gigzz.android.utils.show

class GigzzUserFragment : Fragment(R.layout.fragment_gigzz_user) {
    private val binding by viewBinding(FragmentGigzzUserBinding::bind)
    private val authViewModel by activityViewModels<AuthViewModel>()
    private var isForPermit: Boolean = true
    private var otherType: Boolean = false

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.continueBtn.isEnabled = false
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            validateFields()
        }
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        filePickerLauncher.launch(intent)
    }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    val filePath = getFile(uri)
                    val textV = if (isForPermit) {
                        binding.uploadPermitAutocomplete
                    } else {
                        binding.autocompleteViewResume
                    }
                    textV.setText(filePath)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item,
            genderOptions
        )
        binding.exposedAutocompleteGender.setAdapter(adapter)

        initViews()
        clickListeners()
    }

    private fun clickListeners() = binding.run{
        toolProfile.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        uploadPermit.setOnClickListener {
            isForPermit = true
            launchFilePicker()
        }

        uploadResume.setOnClickListener {
            isForPermit = false
            launchFilePicker()
        }

        continueBtn.setOnClickListener {
            authViewModel.jobSeeker.apply {
                fullName = userName.editText?.text?.trim().toString()
                phoneNo = contact.editText?.text?.trim().toString()
                parentEmail = parentMail.editText?.text?.trim().toString()
                bio = edtBrief.editText?.text?.trim().toString()
                countryCode = edtCountryCode.selectedCountryCodeWithPlus
                countryShortCode = edtCountryCode.selectedCountryCodeWithPlus
                workPermitUrl = permit.editText?.text?.trim().toString()
                resumeUrl = resume.editText?.text?.trim().toString()

                when (radioGrp.checkedRadioButtonId) {
                    R.id.under_age -> {
                        ageGroup = 1
                        requestPermissions()
                        findNavController().navigate(R.id.action_gigzzUserFragment_to_basicDetailsFragment)
                    }

                    R.id.over_age -> {
                        ageGroup = 2
                        requestPermissions()
                        findNavController().navigate(R.id.action_gigzzUserFragment_to_basicDetailsAdultFragment)
                    }
                }
            }
            //  if(otherType) viewModel.helpingHand?.gender=etOtherGender.text.toString().trim() else edtGender.editText?.text.toString().trim()


        }
    }

    private fun initViews() = with(binding){
        edtCountryCode.setDefaultCountryUsingNameCode("US")
        edtCountryCode.setCcpClickable(false)
        userName.editText?.addTextChangedListener(textWatcher)
        contact.editText?.addTextChangedListener(textWatcher)
        parentMail.editText?.addTextChangedListener(textWatcher)
        edtGender.editText?.addTextChangedListener(textWatcher)
        edtBrief.editText?.addTextChangedListener(textWatcher)
        permit.editText?.addTextChangedListener(textWatcher)
        resume.editText?.addTextChangedListener(textWatcher)

        parentMail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val email = p0.toString()
                if (!email.isEmailValid())
                    parentMail.error = "Invalid Email"
                else if (email == authViewModel.emailId) parentMail.error =
                    "You cannot enter same emailId as User"
                else parentMail.error = null
            }
        })
        setupSpaceAndLengthRestriction(editName, 2, 30)
        radioGrp.setOnCheckedChangeListener { _, _ ->
            when (radioGrp.checkedRadioButtonId) {
                R.id.under_age -> {
                    layoutMinor.show()
                    layoutMajor.hide()
                }

                R.id.over_age -> {
                    layoutMajor.show()
                    layoutMinor.hide()
                }
            }
        }

        exposedAutocompleteGender.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    if (s.toString().trim() == "Other (please specify)") {
                        binding.otherGender.show()
                        otherType = true
                        authViewModel.jobSeeker.gender = etOtherGender.text.toString().trim()

                    } else {
                        binding.otherGender.remove()
                        otherType = false
                        authViewModel.jobSeeker.gender = edtGender.editText?.text.toString().trim()
                    }
                }
            }

        })


        describeYourself.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val maxLength = 250
                if (s.length > maxLength) {
                    val trimmedText = s.toString().substring(0, maxLength)
                    describeYourself.setText(trimmedText)
                    describeYourself.setSelection(trimmedText.length)
                    describeYourself.error = "Maximum length reached"
                } else describeYourself.error = null
            }
        })
    }

    private fun validateFields() {
        val username = binding.userName.editText?.restrictNameInput().toString().trim()
        val contact = binding.contact.editText?.text.toString().trim()
        val parentsMail = binding.parentMail.editText?.text.toString().trim()
        val gender = binding.edtGender.editText?.text.toString().trim()
        val permit = binding.permit.editText?.text.toString().trim()
        val bio = binding.edtBrief.editText?.text.toString().trim()
        val resume = binding.resume.editText?.text.toString().trim()
        val validPhone = contact.isPhoneNumberValid(binding.yourContact) && contact.isNotEmpty()

        val allFieldsFilled =
            (username.isNotEmpty() && gender.isNotEmpty() && permit.isNotEmpty() && resume.isNotEmpty() && validPhone)
                    || (username.isNotEmpty() && gender.isNotEmpty() && bio.isNotEmpty() && validPhone && parentsMail.isNotEmpty())
        binding.continueBtn.isEnabled = allFieldsFilled
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            101
        )
    }

    private fun getFile(uri: Uri?): String {
        var result = ""
        if (uri != null) {
            val contentResolver = requireActivity().contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    } else {
                        val uriPath = uri.path
                        if (uriPath != null) {
                            val uriSegments = uriPath.split('/')
                            if (uriSegments.isNotEmpty()) {
                                result = uriSegments.last()
                            }
                        }
                    }
                }
            }
        }
        return result
    }
}