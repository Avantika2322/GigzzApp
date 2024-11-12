package com.gigzz.android.ui.auth.jobseeker

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentBasicDetailsBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.ui.auth.OpportunitiesListAdapter
import com.gigzz.android.utils.LocationHelper
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.isZipCodeValid
import com.gigzz.android.utils.showToast
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.chip.Chip
import java.io.IOException
import java.util.Calendar


class BasicDetailsFragment : Fragment(R.layout.fragment_basic_details) {
    private val binding by viewBinding(FragmentBasicDetailsBinding::bind)
   // private lateinit var skillsListAdapter: OpportunitiesListAdapter
    private lateinit var interestsListAdapter: OpportunitiesListAdapter
    private val authViewModel: AuthViewModel by activityViewModels()
    private var allFieldsFilled: Boolean = false
    private var latitude = ""
    private var longitude = ""
   // private val skillList = ArrayList<String>()
   // private val interestList = ArrayList<String>()
   // private val selectedSkills = arrayListOf<String>()
   // private val selectedInterest = arrayListOf<String>()

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.continueBtn.isEnabled = true
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            validateFields()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         authViewModel.getMasterDataApi()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setClickListener()
        //getMasterDataObserver()
    }

    private fun initViews() = with(binding) {
        /*skillsListAdapter =
            OpportunitiesListAdapter(skillList) { selectedItem, isChecked ->
                onSkillClickChip(selectedItem, isChecked)
            }
        binding.recyclerViewSkills.adapter = skillsListAdapter*/

       /* interestsListAdapter =
            OpportunitiesListAdapter(interestList) { selectedItem, isChecked ->
                onInterestClick(selectedItem, isChecked)
            }
        binding.recyclerViewInterests.adapter = interestsListAdapter*/

        edtAddress.editText?.addTextChangedListener(textWatcher)
        edtZipcode.editText?.addTextChangedListener(textWatcher)
        edtDateOfBirth.editText?.addTextChangedListener(textWatcher)

        edtZipcode.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                val zipcode = p0.toString()
                if (!zipcode.isZipCodeValid())
                    edtZipcode.error = "Invalid ZipCode"
                else edtZipcode.error = null
            }
        })
        //  edtInterest.editText?.addTextChangedListener(textWatcher)
    }

    private fun setClickListener() = binding.run {
        edtAddress.editText?.setOnClickListener { autoCompleteSupportGoogleApi() }
        interests.setOnClickListener {
            selectSkills.setImageResource(R.drawable.downarrow)
            cardRecyclerSkills.isVisible = false
            if (cardRecyclerInterest.isVisible) {
                selectInterest.setImageResource(R.drawable.downarrow)
                cardRecyclerInterest.isVisible = false
            } else {
                selectInterest.setImageResource(R.drawable.arrow_up)
                cardRecyclerInterest.isVisible = true
            }
        }

        skills.setOnClickListener {
            selectInterest.setImageResource(R.drawable.downarrow)
            cardRecyclerInterest.isVisible = false
            if (cardRecyclerSkills.isVisible) {
                selectSkills.setImageResource(R.drawable.downarrow)
                cardRecyclerSkills.isVisible = false
            } else {
                selectSkills.setImageResource(R.drawable.arrow_up)
                cardRecyclerSkills.isVisible = true
            }
        }
        toolProfile.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        editDob.setOnClickListener {
            showDatePicker()
        }

        continueBtn.setOnClickListener {
            if (validation()) {
                authViewModel.jobSeeker.apply {
                    address = edtAddress.editText?.text?.trim().toString()
                    lat = latitude
                    lon = longitude
                    zipCode = edtZipcode.editText?.text?.trim().toString()
                    dob = edtDateOfBirth.editText?.text?.trim().toString()
                    //interests = selectedInterest
                    //skills = selectedSkills
                }
                findNavController().navigate(R.id.action_basicDetailsFragment_to_skillsFragment)
            }

            /*if (selectedSkills.isEmpty()) showToast(requireContext(),"Select at least 1 Skill")
            else if (selectedInterest.isEmpty()) showToast(requireContext(), "Select at least 1 Interest")
            else {
                if (validation()) {
                    authViewModel.jobSeeker.apply {
                        address = edtAddress.editText?.text?.trim().toString()
                        lat = latitude
                        lon = longitude
                        zipCode = edtZipcode.editText?.text?.trim().toString()
                        dob = edtDateOfBirth.editText?.text?.trim().toString()
                        interests = selectedInterest
                        //skills = selectedSkills
                    }
                    findNavController().navigate(R.id.action_basicDetailsFragment_to_profileSetUpFragment, bundleOf("from" to "jobSeeker"))
                }
            }*/
        }
    }

    /*private fun onSkillClickChip(chipText: String, isChecked: Boolean) {
        if (isChecked) {
            val chip = Chip(requireContext())
            chip.text = chipText
            chip.tag = chipText
            chip.isCloseIconVisible = true
            chip.setTextColor(requireActivity().getColor(R.color.theme_blue))
            chip.chipBackgroundColor =
                ColorStateList.valueOf(requireActivity().getColor(R.color.light_blue_shade_1))
            chip.setOnCloseIconClickListener {
                val tag = it.tag as? String
                tag?.let { removedItem ->
                    binding.chipGroupSkills.removeView(it)
                    selectedSkills.remove(removedItem)
                    val position = skillList.indexOf(removedItem)
                    skillsListAdapter.notifyItemInserted(skillList.size)
                    if (position != -1) {
                        skillsListAdapter.deselectItem(chipText)
                    }
                }
                checkAndToggleTextVisibility()
            }
            selectedSkills.add(chipText)
            binding.chipGroupSkills.addView(chip)
        }
        checkAndToggleTextVisibility()
    }*/

   /* private fun onInterestClick(chipText: String, isChecked: Boolean) {
        if (isChecked) {
            val chip = Chip(requireContext())
            chip.text = chipText
            chip.isCloseIconVisible = true
            chip.setTextColor(requireActivity().getColor(R.color.theme_blue))
            chip.chipBackgroundColor =
                ColorStateList.valueOf(requireActivity().getColor(R.color.light_blue_shade_1))
            chip.tag = chipText
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                val tag = it.tag as? String
                tag?.let { removedItem ->
                    binding.chipGroupInterest.removeView(it)
                    selectedInterest.remove(removedItem)
                    val position = interestList.indexOf(removedItem)
                    interestsListAdapter.notifyItemInserted(interestList.size)
                    if (position != -1) {
                        interestsListAdapter.deselectItem(chipText)
                    }
                }
                checkAndToggleTextVisibility()
            }
            selectedInterest.add(chipText)
            binding.chipGroupInterest.addView(chip)
        }
        checkAndToggleTextVisibility()
    }*/

    private fun validateFields() {
        val address = binding.edtAddress.editText?.text.toString().trim()
        val zipcode = binding.edtZipcode.editText?.text.toString().trim()
        val dob = binding.edtDateOfBirth.editText?.text.toString().trim()
       // val interests = viewModel.helpingHand?.interests
       // val skills = viewModel.helpingHand?.skills
        val validZipCode = zipcode.isNotEmpty() && zipcode.isZipCodeValid()
        allFieldsFilled =
            address.isNotEmpty() && validZipCode && dob.isNotEmpty() /*&& interests?.isNotEmpty() == true && skills?.isNotEmpty() == true*/
    }

/*    private fun checkAndToggleTextVisibility() {
        binding.continueBtn.isEnabled = allFieldsFilled
        binding.skillsText.isVisible = binding.chipGroupSkills.childCount <= 0
        if (binding.chipGroupSkills.childCount <= 0) skillsListAdapter.clearAllSelections()
    }*/

    private fun validation(): Boolean {
        val address = binding.edtAddress.editText?.text.toString().trim()
        val zipcode = binding.edtZipcode.editText?.text.toString().trim()
        val dob = binding.edtDateOfBirth.editText?.text.toString().trim()
        /*val interests = selectedInterest
        val skills = selectedInterest*/
        binding.apply {
            when {
                address.isEmpty() -> {
                    edtAddress.error = getString(R.string.enter_your_address)
                    return false
                }

                zipcode.isEmpty() -> {
                    edtZipcode.error = getString(R.string.enter_zipcode)
                    return false
                }

                !zipcode.isZipCodeValid() -> {
                    edtZipcode.error = getString(R.string.enter_valid_zipcode)
                    return false
                }

                dob.isEmpty() -> {
                    edtDateOfBirth.error = getString(R.string.enter_dob)
                    return false
                }

                /*interests.isEmpty() -> {
                    showToast(requireContext(),"Please select at least one interest")
                    return false
                }

                skills.isEmpty() -> {
                    showToast(requireContext(),"Please select at least one skill")
                    return false
                }*/

                else -> return true
            }
        }

    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this.requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedMonth + 1}-$selectedDay-$selectedYear"
                binding.editDob.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun getMasterDataObserver() {
        authViewModel.masterDataResLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    it.data?.data?.let {data->
                       /* if(!data.skills.isNullOrEmpty()) {
                            skillList.clear()
                            for (i in 0 until data.skills.size) {
                                if (!skillList.contains(data.skills[i].skill_name)) {
                                    skillList.add(data.skills[i].skill_name)
                                }
                            }
                            skillsListAdapter.notifyItemInserted(skillList.size)
                        }*/

                       /* if(!data.passion.isNullOrEmpty()) {
                            interestList.clear()
                            for (i in 0 until data.passion.size) {
                                if (!interestList.contains(data.passion[i].passion)) {
                                    interestList.add(data.passion[i].passion)
                                }
                            }
                            interestsListAdapter.notifyItemInserted(interestList.size)
                        }*/
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
                                    binding.edtAddress.editText?.setText(fullAddress)
                                    binding.edtZipcode.editText?.setText(address.postalCode)
                                    authViewModel.jobSeeker.apply{
                                        city = if (address.locality!=null) address.locality.toString() else "NA"
                                        state=if (address.adminArea!= null) address.adminArea else "NA"
                                    }
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