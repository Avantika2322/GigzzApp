package com.gigzz.android.ui.auth.company

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentCompanyJobGiverProfileBinding
import com.gigzz.android.databinding.FragmentCompanyPostedJobsBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.utils.LocationHelper
import com.gigzz.android.utils.isZipCodeValid
import com.gigzz.android.utils.restrictNameInput
import com.gigzz.android.utils.restrictOrganizationNameInput
import com.gigzz.android.utils.setupSpaceAndLengthRestriction
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.IOException


class CompanyJobGiverProfileFragment : Fragment(R.layout.fragment_company_job_giver_profile) {
    private val binding by viewBinding(FragmentCompanyJobGiverProfileBinding::bind)
    private var latitude = ""
    private var longitude = ""
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val industryTypes =
            arrayOf("Retail", "Fast Food", "Administrative", "Camp Position", "Other")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, industryTypes)
        binding.autoCompleteTextView.setAdapter(adapter)
        binding.autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedIndustry = adapter.getItem(position).toString()
                validateFields(selectedIndustry)
            }

        initViews()
        setClickListener()
    }

    private fun initViews() = with(binding) {
        cname.editText?.addTextChangedListener(textWatcher)
        jobAddress.editText?.addTextChangedListener(textWatcher)
        edtZipCode.editText?.addTextChangedListener(textWatcher)
        hiringName.editText?.addTextChangedListener(textWatcher)
        otherItEditText.addTextChangedListener(textWatcher)
    }

    private fun setClickListener() = binding.run {
        jobAddress.editText?.setOnClickListener {
            autoCompleteSupportGoogleApi()
        }
        toolCompanyProfile.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        edtZipCode.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val zipcode = p0.toString()
                if (!zipcode.isZipCodeValid())
                    edtZipCode.error = "Invalid ZipCode"
                else edtZipCode.error = null
            }

        })
        setupSpaceAndLengthRestriction(binding.editCname, 2, 30)
        setupSpaceAndLengthRestriction(binding.edtHiringName, 2, 30)
        continueBtn.setOnClickListener {
            viewModel.companyReq.apply {
                lat = latitude
                lon = longitude
                companyName = cname.editText?.text?.trim().toString()
                jobDescription = companyDes.editText?.text?.trim().toString()
                address = jobAddress.editText?.text?.trim().toString()
                zipCode = edtZipCode.editText?.text?.trim().toString()
                hiringName = edtHiringName.text?.trim().toString()
            }

            findNavController().navigate(
                R.id.action_companyJobGiverProfileFragment_to_profileSetUpFragment,
                bundleOf("from" to "CompanySignUp")
            )
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.continueBtn.isEnabled = false
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            validateFields(binding.autoCompleteTextView.text.toString())
        }
    }

    private fun validateFields(selectedIndustry: String) {
        val isOtherSelected = selectedIndustry == "Other"
        val isOtherEditTextFilled = binding.otherItEditText.text.toString().isNotEmpty()

        if (isOtherSelected) {
            binding.otherIndustryType.visibility = View.VISIBLE
            viewModel.companyReq.industryType = listOf(binding.otherItEditText.text.toString())
        } else {
            binding.otherIndustryType.visibility = View.GONE
            viewModel.companyReq.industryType =
                listOf(binding.autoCompleteTextView.text?.trim().toString())

        }
        val name = binding.cname.editText?.restrictOrganizationNameInput().toString().trim()
        val companyDes = binding.companyDes.editText?.toString()?.trim()
//        val name = setupSpaceAndLengthRestriction(binding.editCname, 2, 30).toString()

        val address = binding.jobAddress.editText?.text.toString().trim()
        val zipcode = binding.edtZipCode.editText?.text.toString().trim()
        val hiringName = binding.hiringName.editText?.restrictNameInput().toString().trim()
//        val hiringName = setupSpaceAndLengthRestriction(binding.edtHiringName, 2, 30).toString()
        val validZipCode = zipcode.isNotEmpty() && zipcode.isZipCodeValid()


        val allFieldsFilled =
            name.isNotEmpty() && address.isNotEmpty() && validZipCode && hiringName.isNotEmpty() && companyDes?.isNotEmpty() == true
//        binding.continueBtn.isEnabled = allFieldsFilled
        updateContinueButton(isOtherSelected, isOtherEditTextFilled, allFieldsFilled)
    }

    private fun updateContinueButton(
        isOtherSelected: Boolean,
        isOtherEditTextFilled: Boolean,
        isAllFieldsFilled: Boolean,
    ) {
        binding.continueBtn.isEnabled =
            (isAllFieldsFilled && isOtherSelected && isOtherEditTextFilled) ||
                    (isAllFieldsFilled && !isOtherSelected)
    }

    private fun autoCompleteSupportGoogleApi() {
        // Fetching API_KEY which we wrapped
        val apiKey = getString(R.string.places_api_key)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext().applicationContext, apiKey)
        }

        val fields = listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.LAT_LNG,
            Place.Field.OPENING_HOURS,
            Place.Field.RATING,
            Place.Field.USER_RATINGS_TOTAL

        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(requireActivity())
        placeResultLauncher.launch(intent)
    }

    private val locationHelper: LocationHelper by lazy {
        LocationHelper(requireContext(), requireActivity(), null)
    }

    private val placeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                AutocompleteActivity.RESULT_OK -> {
                    result.data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        place.latLng?.let { latLng ->
                            val selectedAddress = locationHelper.getAddress(
                                context = requireContext(),
                                lat = latLng.latitude,
                                lng = latLng.longitude
                            )
                            latitude = latLng.latitude.toString()
                            longitude = latLng.longitude.toString()
                            try {
                                selectedAddress?.let { address ->
                                    val fullAddress = address.getAddressLine(0).toString()
                                    binding.jobAddress.editText?.setText(
                                        fullAddress
                                    )
                                    viewModel.companyReq.apply {
                                        city =
                                            if (address.locality != null) address.locality.toString() else "NA"
                                        state =
                                            if (address.adminArea != null) address.adminArea else "NA"
                                    }
                                    binding.edtZipCode.editText?.setText(address.postalCode)
                                    binding.jobAddress.editText?.length()?.let { it1 ->
                                        binding.jobAddress.editText?.setSelection(it1)
                                    }
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                }

                AutocompleteActivity.RESULT_CANCELED -> {
                    //
                }
            }
            return@registerForActivityResult
        }
}