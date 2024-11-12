package com.gigzz.android.ui.auth.individual

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentIndividualJobGiverBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.utils.LocationHelper
import com.gigzz.android.utils.isPhoneNumberValid
import com.gigzz.android.utils.isZipCodeValid
import com.gigzz.android.utils.restrictNameInput
import com.gigzz.android.utils.setupSpaceAndLengthRestriction
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.IOException


class IndividualJobGiverProfileFragment : Fragment(R.layout.fragment_individual_job_giver) {
    private val binding by viewBinding(FragmentIndividualJobGiverBinding::bind)
    private val viewModel: AuthViewModel by activityViewModels()
    private var latitude = ""
    private var longitude = ""

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setClickListener()
    }

    private fun initViews() = with(binding) {
        edtCountryCode.setCcpClickable(false)
        edtCountryCode.setDefaultCountryUsingNameCode("US")
        username.editText?.addTextChangedListener(textWatcher)
        contact.editText?.addTextChangedListener(textWatcher)
        edtAddress.editText?.addTextChangedListener(textWatcher)
        edtZipCode.editText?.addTextChangedListener(textWatcher)
        edtDescription.editText?.addTextChangedListener(textWatcher)

        edtZipCode.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val zipcode = p0.toString()
                if (!zipcode.isZipCodeValid()) edtZipCode.error = "Invalid ZipCode"
                else edtZipCode.error = null
            }
        })
        edtDescription.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

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

    private fun setClickListener() = binding.run {
        edtAddress.editText?.setOnClickListener { autoCompleteSupportGoogleApi() }
        navigationIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        setupSpaceAndLengthRestriction(editName, 2, 30)
        edtZipCode.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val zipcode = p0.toString()
                if (!zipcode.isZipCodeValid()) edtZipCode.error = "Invalid ZipCode"
                else edtZipCode.error = null
            }

        })
        edtDescription.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

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

        continueBtn.setOnClickListener {
            viewModel.individualJobGiverReq.apply {
                fullName = username.editText?.text?.trim().toString()
                countryCode = edtCountryCode.selectedCountryCodeWithPlus
                phoneNo = contact.editText?.text?.trim().toString()
                address = edtAddress.editText?.text?.trim().toString()
                zipCode = edtZipCode.editText?.text?.trim().toString()
                description = edtDescription.editText?.text?.trim().toString()
                lat = latitude
                lon = longitude
            }
            findNavController().navigate(R.id.action_individualJobGiverProfileFragment_to_opportunitiesFragment)
        }
    }

    private fun validateFields() {
        val username = binding.username.editText?.restrictNameInput().toString().trim()
        val contact = binding.contact.editText?.text.toString().trim()
        val address = binding.edtAddress.editText?.text.toString().trim()
        val zipcode = binding.edtZipCode.editText?.text.toString().trim()
        val bio = binding.edtDescription.editText?.text.toString().trim()
        val validUserName = username.isNotEmpty() && username.length in 3..30
        val validPhone = contact.isPhoneNumberValid(binding.contactEdt) && contact.isNotEmpty()
        val validZipCode = zipcode.isNotEmpty() && zipcode.isZipCodeValid()

        val allFieldsFilled =
            validUserName && validPhone && address.isNotEmpty() && validZipCode && bio.isNotEmpty()
        binding.continueBtn.isEnabled = allFieldsFilled
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
                                    binding.edtAddress.editText?.setText(
                                        fullAddress
                                    )
                                    viewModel.individualJobGiverReq.apply{
                                        city = if (address.locality!=null) address.locality.toString() else "NA"
                                        state=if (address.adminArea!= null) address.adminArea else "NA"
                                    }
                                    binding.edtZipCode.editText?.setText(address.postalCode)
                                    binding.edtAddress.editText?.length()?.let { it1 ->
                                        binding.edtAddress.editText?.setSelection(it1)
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