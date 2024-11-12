package com.gigzz.android.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentEditProfileBinding
import com.gigzz.android.domain.req.EditProfileReq
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.utils.LocationHelper
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.isPhoneNumberValid
import com.gigzz.android.utils.isZipCodeValid
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.launch
import java.io.IOException


class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    private val binding by viewBinding(FragmentEditProfileBinding::bind)
    private val profileViewModel by hiltNavGraphViewModels<ProfileViewModel>(R.id.my_profile_nav_graph)
    var lat = ""
    var long = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initViews()
        setClickListener()
        textWatchers()
        handleGetProfileResponse()
    }

   /* private fun initViews() = with(binding) {
        if (isInternetAvailable()) viewModel.getUserProfile()
        else toast(R.string.no_internet)

    }*/

    private fun handleGetProfileResponse() {
        profileViewModel.getMyProfileRes.observe(viewLifecycleOwner) { res ->
            binding.apply {
                res?.data?.data?.let {
                    when (it.userType) {
                        1 -> {
                            userBio.editText?.setText(it.bio)
                            userPhone.editText?.setText(it.phoneNumber)
                        }

                        2 -> {
                            userBio.editText?.setText(it.description)
                            userPhone.editText?.setText(it.phoneNumber)
                            userEmail.show()
                            userEmail.editText?.setText(it.emailId)
                        }

                        3 -> {
                            userBio.editText?.setText(it.jobDescription)
                            userPhone.remove()
                            userEmail.show()
                            userEmail.editText?.setText(it.emailId)
                            userPhone.editText?.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_mail,
                                0,
                                0,
                                0
                            )
                            userEmail.editText?.setText(it.emailId)

                        }
                    }

                    userAddress.editText?.setText(it.address)
                    userState.editText?.setText(it.address?.substringAfterLast(","))
                    userPostcard.editText?.setText(it.zipCode)
                    lat = it.lat.toString()
                    long = it.lon.toString()
                }
            }

        }
    }

    private fun setClickListener() = binding.run {
        backBtn.setOnClickListener { findNavController().popBackStack() }
        etAddress.setOnClickListener { autoCompleteSupportGoogleApi() }
        saveBtn.setOnClickListener {
            if (validation()) {
                var image=""
                lifecycleScope.launch {
                    image = profileViewModel.getUserImg().toString()
                }

                val model = EditProfileReq(
                    address = userAddress.editText?.text.toString().trim(),
                    phoneNumber = userPhone.editText?.text.toString().trim(),
                    zipCode = userPostcard.editText?.text.toString().trim(),
                    bio = userBio.editText?.text.toString().trim(),
                    lat = lat,
                    lon = long,
                    profileImage = image
                )
                profileViewModel.editProfileApi(model)
                handleEditProfileResponse()
            }
        }
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
                            lat = latLng.latitude.toString()
                            long = latLng.longitude.toString()
                            try {
                                selectedAddress?.let { address ->
                                    val fullAddress = address.getAddressLine(0).toString()
                                    binding.userAddress.editText?.setText(fullAddress)
                                    binding.userPostcard.editText?.setText(address.postalCode)
                                    binding.userState.editText?.setText(address.countryName)
                                    binding.userAddress.editText?.length()?.let { it1 ->
                                        binding.userAddress.editText?.setSelection(it1)
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

                }
            }
            return@registerForActivityResult
        }

    private fun validation(): Boolean {
        with(binding) {
            val address = userAddress.editText?.text.toString().trim()
            val bio = userBio.editText?.text.toString().trim()
            val phone = userPhone.editText?.text.toString().trim()
            val postCard = userPostcard.editText?.text.toString().trim()
            val state = userState.editText?.text.toString().trim()

            when {
                address.isEmpty() -> {
                    userAddress.error = resources.getString(R.string.enter_your_address)
                    return false
                }

                bio.isEmpty() -> {
                    userBio.error = resources.getString(R.string.enter_bio)
                    return false
                }

                postCard.isEmpty() -> {
                    userPostcard.error = resources.getString(R.string.enter_zipcode)
                    return false
                }

                !postCard.isZipCodeValid() -> {
                    userPostcard.error = resources.getString(R.string.enter_valid_zipcode)
                    return false
                }

                state.isEmpty() -> {
                    userState.error = resources.getString(R.string.enter_state_name)
                    return false
                }

                phone.isEmpty() -> {
                    if (profileViewModel.userType != 3) {
                        userPhone.error = resources.getString(R.string.enter_your_phone)
                        return false
                    } else return true
                }

                !phone.isPhoneNumberValid(null) -> {
                    if (profileViewModel.userType != 3) {
                        userPhone.error = resources.getString(R.string.enter_your_valid_phone)
                        return false
                    } else return true
                }

                else -> return true

            }
        }
    }

    private fun textWatchers() = binding.run {
        userAddress.editText?.addTextChangedListener(address)
        userBio.editText?.addTextChangedListener(bio)
        userPhone.editText?.addTextChangedListener(phone)
        userPostcard.editText?.addTextChangedListener(postcard)
        userState.editText?.addTextChangedListener(state)
    }

    private val address = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.userAddress.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.userAddress.error = resources.getString(R.string.enter_your_address)
            } else {
                binding.userAddress.error = null
            }
        }
    }
    private val bio = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.userBio.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.userBio.error = resources.getString(R.string.enter_bio)
            } else {
                binding.userBio.error = null
            }
        }
    }
    private val phone = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.userPhone.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.userPhone.error = resources.getString(R.string.enter_your_phone)
            } else if (p0?.substring(0, 1)?.toInt() == 0) {
                binding.userPhone.error = resources.getString(R.string.enter_your_valid_phone)
            } else {
                binding.userPhone.error = null
            }
        }
    }
    private val postcard = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.userPostcard.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.userPostcard.error = resources.getString(R.string.enter_zipcode)
            } else {
                binding.userPostcard.error = null
            }
        }
    }
    private val state = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.userState.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.userState.error = resources.getString(R.string.enter_state_name)
            } else {
                binding.userState.error = null
            }
        }
    }

    private fun handleEditProfileResponse() {
        profileViewModel.editUserProfileRes.observe(viewLifecycleOwner){
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