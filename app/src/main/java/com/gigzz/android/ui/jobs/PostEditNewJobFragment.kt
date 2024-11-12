package com.gigzz.android.ui.jobs

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentPostEditNewJobBinding
import com.gigzz.android.domain.req.Image
import com.gigzz.android.domain.req.Medias
import com.gigzz.android.domain.req.PostNewJobByCompany
import com.gigzz.android.domain.res.CompanyJobData
import com.gigzz.android.presentation.JobsViewModel
import com.gigzz.android.presentation.ProfileViewModel
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.LocationHelper
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.convertToFile
import com.gigzz.android.utils.hasPermission
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.hideKeyboard
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.resize
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

@AndroidEntryPoint
class PostEditNewJobFragment : Fragment(R.layout.fragment_post_edit_new_job) {
    private val binding by viewBinding(FragmentPostEditNewJobBinding::bind)
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var mediaAdapter: PostJobImagesAdapter
    private var ageGroup: Int = 0
    private var jobType: String = "1"
    lateinit var imageUrl: String
    private var categoryName: String = ""
    private var mediaList = ArrayList<Medias>()
    private var categoryList = ArrayList<String>()
    private var postReqList = ArrayList<Image>()
    private val MAX_IMAGES = 5
    private var selectedImageCount = 0
    var latitude = 0.0
    var longitude = 0.0
    private var isEdit = false
    private var cropCount = MutableLiveData<Int>()
    private var currentJobId: Int? = -1

    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            binding.progressBar.remove()
            if ((uris?.size ?: 0) <= MAX_IMAGES - selectedImageCount) {
                // If the total number of selected images does not exceed the limit, continue with cropping
                uris?.forEachIndexed {index, uri ->
                    cropCount.observe(this, Observer {
                        if (cropCount.value==index){
                            cropImage(uri)
                            selectedImageCount++
                        }
                    })
                }
            } else {
                showToast(requireContext(),"Maximum $MAX_IMAGES images can be selected")
                selectedImageCount = 0
            }
        }

    private val cropImageLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        binding.progressBar.remove()
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                val uri = UCrop.getOutput(it)
                val bitmap =
                    MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        uri
                    )
                val resizedImage = resize(bitmap, 1024, 1024)
                val file = getImageFile(resizedImage)
                cropCount.value = cropCount.value!! + 1
                if (mediaList.size < 5) {
                    mediaList.add(
                        mediaList.size,
                        Medias(
                            mediaType = "I",
                            mediaUrl = file,
                            mediaThumbnail = file,
                        )
                    )
                }
                binding.rvPhotos.show()
                binding.layoutImage.visibility = View.GONE
                binding.tvUploadPhoto.visibility = View.GONE
                setAdapters()
                mediaAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getMasterDataApi()
        arguments?.apply {
            isEdit = getBoolean("isFromEdit")
            currentJobId = getInt("jobId")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cropCount.value=0
        setClickListener()
        textWatchers()
        initViews()
        handleMasterDataRes()
        setAdapters()
    }

    private fun setAdapters() {
        mediaAdapter =
            PostJobImagesAdapter(
                requireContext(),
                mediaList,
            ) {pos,model,src ->
                onImageClick(pos,model,src)
            }
        binding.rvPhotos.adapter = mediaAdapter
    }

    private fun initViews() = with(binding) {
        etCompanyName.setText(JobsFragment.companyName)
        etSalary.filters = arrayOf(DecimalDigitsInputFilter(4, 1))

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                resources.getStringArray(R.array.experience_required)
            )
        etExperience.threshold = 3
        etExperience.setAdapter(adapter)
        etExperience.setOnItemClickListener { adapterView, view, i, l ->
            if (adapterView.getItemAtPosition(i)
                    .toString() == "Yes"
            ) etEnterExperienceLayout.show() else etEnterExperienceLayout.remove()
        }

        if (isEdit) handleJobDetailRes()
    }

    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }

    private fun setClickListener() = binding.run {
        ivBack.setOnClickListener { findNavController().popBackStack() }
        etAddress.setOnClickListener { autoCompleteSupportGoogleApi() }
        radioGroupAge.setOnCheckedChangeListener { _, _ ->
            when (radioGroupAge.checkedRadioButtonId) {
                R.id.under_age -> ageGroup = 0
                R.id.mid_age -> ageGroup = 1
                R.id.over_age -> ageGroup = 2
            }
        }
        radioGroupJobType.setOnCheckedChangeListener { _, _ ->
            when (radioGroupJobType.checkedRadioButtonId) {
                R.id.radioFullTime -> jobType = "1"
                R.id.radioPartTime -> jobType = "2"
                R.id.radioProject -> jobType = "3"
            }
        }

        tvTakePhoto.setOnClickListener {
            captureLauncher.launch(
                ImagePicker.with(requireActivity())
                    .cameraOnly()
                    .createIntent()
            )
        }
        tvUploadPhoto.setOnClickListener {
            pickImages()
            // imageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnFollow.setOnClickListener {
            if (validation()) {
                createPathForS3()
                if (etExperience.text.toString()
                        .trim() == "No"
                ) etEnterExperience.setText(getString(R.string.no))
                else if (etExperience.text.toString()
                        .trim() != "Yes" && etExperience.text.toString().trim() != "No"
                ) etEnterExperience.setText(etExperience.text.toString().trim())
                val salary =
                    if (etSalary.text.toString().trim().isNotEmpty() && etSalary.text.toString()
                            .trim().contains(".")
                    ) {
                        when (etSalary.text.toString().trim().substringAfter(".").length) {
                            2 -> etSalary.text.toString().trim()
                            else -> etSalary.text.toString().trim() + "0"
                        }
                    } else etSalary.text.toString().trim() + ".00"

                val jobTypesList = arrayListOf<Int>(JobsFragment.jobType!!)

                if (isEdit) {
                    val model = PostNewJobByCompany(
                        age_requirement = ageGroup,
                        category_name = etCategory.text.toString().trim(),
                        company_name = etCompanyName.text.toString().trim(),
                        experience = etExperience.text.toString().trim(),
                        images = postReqList,
                        job_types = jobTypesList,
                        address = etAddress.text.toString().trim(),
                        job_title = etJobName.text.toString().trim(),
                        lat = latitude.toDouble(),
                        lon = longitude.toDouble(),
                        zip_code = etZipcode.text.toString().trim(),
                        salary = salary,
                        job_id = currentJobId!!.toInt(),
                        description = etJobDesc.text.toString().trim(),
                        company_job_url = etWebUrl.text.toString().trim(),
                        job_location = if (radioGroupLocation.checkedRadioButtonId == R.id.radioOnsite) 1 else 0,
                        post_type = "J"
                    )
                    jobsViewModel.editJobAsCompany(model)
                    handlePostJobByCompanyResponse()

                } else {
                    val model = PostNewJobByCompany(
                        age_requirement = ageGroup,
                        category_name = etCategory.text.toString().trim(),
                        company_name = etCompanyName.text.toString().trim(),
                        experience = etExperience.text.toString().trim(),
                        images = postReqList,
                        job_types = jobTypesList,
                        address = etAddress.text.toString().trim(),
                        job_title = etJobName.text.toString().trim(),
                        lat = latitude.toDouble(),
                        lon = longitude.toDouble(),
                        zip_code = etZipcode.text.toString().trim(),
                        salary = salary,
                        job_id = null,
                        description = etJobDesc.text.toString().trim(),
                        company_job_url = etWebUrl.text.toString().trim(),
                        job_location = if (radioGroupLocation.checkedRadioButtonId == R.id.radioOnsite) 1 else 0,
                        post_type = "J"
                    )
                    jobsViewModel.postNewJobByCompany(model)
                    handlePostJobByCompanyResponse()
                    // postReqList.clear()
                }
            }
        }

    }

    private fun validation(): Boolean {
        with(binding) {
            val companyName = etCompanyName.text.toString().trim()
            val jobTitle = etJobName.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val experience = etExperience.text.toString().trim()
            val experienceValue = etEnterExperience.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val salary = etSalary.text.toString().trim()
            val description = etJobDesc.text.toString().trim()

            when {
                companyName.isEmpty() -> {
                    etCompanyName.error = resources.getString(R.string.enter_company_name)
                    return false
                }

                jobTitle.isEmpty() -> {
                    etJobName.error = resources.getString(R.string.enter_job_title)
                    return false
                }

                category.isEmpty() -> {
                    etCategory.error = resources.getString(R.string.select_category)
                    return false
                }

                /* category == "Other" -> {
                     if (binding.editOthers.editText?.text.toString().trim().isEmpty()) {
                         binding.editOthers.error = "Write your category"
                         return false
                     } else {
                         categoryName = binding.editOthers.editText?.text.toString().trim()
                         return true
                     }
                 }*/

                experience.isEmpty() -> {
                    etExperience.error = resources.getString(R.string.choose_experience)
                    return false
                }

                experienceValue.isEmpty() && experience == "Yes" -> {
                    etEnterExperience.error = resources.getString(R.string.enter_year_of_experience)
                    return false
                }

                address.isEmpty() -> {
                    etAddress.error = resources.getString(R.string.enter_company_address)
                    return false
                }

                salary.isEmpty() -> {
                    etSalary.error = resources.getString(R.string.enter_salary)
                    return false
                }

                salary.toDouble() > 10000F -> {
                    etSalary.error = resources.getString(R.string.enter_valid_salary)
                    return false
                }

                description.isEmpty() -> {
                    etJobDesc.error = resources.getString(R.string.enter_description)
                    return false
                }

                else -> return true
            }
        }
    }

    private fun textWatchers() = binding.run {
        etCompanyName.addTextChangedListener(companyName)
        etJobName.addTextChangedListener(jobTitle)
        etJobDesc.addTextChangedListener(description)
        etExperience.addTextChangedListener(experience)
        etAddress.addTextChangedListener(location)
        etSalary.addTextChangedListener(salary)
        etCategory.addTextChangedListener(category)
    }

    private val companyName = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etCompanyName.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etCompanyName.error = resources.getString(R.string.enter_company_name)
            } else {
                binding.etCompanyName.error = null
            }
        }
    }
    private val jobTitle = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etJobName.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etJobName.error = resources.getString(R.string.enter_job_title)
            } else {
                binding.etJobName.error = null
            }
        }
    }
    private val experience = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etExperience.error = null
            if (p0?.contains(".") == true) binding.etExperience.filters =
                arrayOf(InputFilter.LengthFilter(3))
            else binding.etExperience.filters = arrayOf(InputFilter.LengthFilter(2))
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etExperience.error =
                    resources.getString(R.string.enter_year_of_experience)
            } else if (p0?.contains("0") == true && !p0.startsWith("0.", 0, false)) {
                binding.etExperience.error =
                    resources.getString(R.string.valid_year_of_experience)
            } else {
                binding.etExperience.error = null
            }
        }
    }
    private val location = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etAddress.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etAddress.error = resources.getString(R.string.enter_company_address)
            } else {
                binding.etAddress.error = null
            }
        }
    }
    private val salary = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etSalary.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etSalary.error = resources.getString(R.string.enter_salary)
            } else if (p0?.length!! >= 2 && p0.substring(0, 2) == ".0") {
                binding.etSalary.error =
                    resources.getString(R.string.less_zero_salary)
            } else if (p0.substring(0, 1) == ".") {
                // binding.editSalary.error = resources.getString(R.string.less_zero_salary)
            } else if (p0.substring(0, 1)?.toDouble() == 0.0) {
                binding.etSalary.error = resources.getString(R.string.valid_salary)
            } else if (p0.toString().toDouble() > 10000F) {
                binding.etSalary.error = resources.getString(R.string.enter_valid_salary)
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
                binding.etJobDesc.error = resources.getString(R.string.enter_description)
            } else {
                binding.etJobDesc.error = null
            }
        }
    }
    private val category = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.etCategory.error = null
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.isEmpty() == true) {
                binding.etCategory.error = resources.getString(R.string.select_category)
            } else {
                binding.etCategory.error = null
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
                                            categoryName = it.category[i].name
                                            if (it.category[i].name == "Other") {
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

    private fun getImageFile(inImage: Bitmap): File {
        val tempFile = File.createTempFile("temp" + mediaList.size, ".png")
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()
        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return tempFile
    }

    private val captureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                val bitmap =
                    MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        uri
                    )

                val file = bitmap.convertToFile(requireActivity())
                if (mediaList.size < 5) {
                    file.let {
                        mediaList.add(
                            mediaList.size,
                            Medias(
                                mediaUrl = file, mediaThumbnail = file
                            )
                        )
                        binding.layoutImage.visibility = View.GONE
                        binding.tvUploadPhoto.visibility = View.GONE
                        mediaAdapter.notifyDataSetChanged()

                    }
                }
            }
        }

    private fun onImageClick(pos: Int, model: Medias?, src: String) {
        when (src) {
            "root" -> {
                /*if (model == null && mediaList.size < 5) {
                    pickImages()
                }*/
            }

            "cross" -> {
                if (pos != mediaList.size) {
                    mediaList.removeAt(pos)
                    mediaAdapter.notifyItemRemoved(pos)
                    if (mediaList.isEmpty()) {
                        binding.layoutImage.visibility = View.VISIBLE
                        binding.tvUploadPhoto.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun createPathForS3() {
        val formatMonth = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val curMonth: String = formatMonth.format(Date(System.currentTimeMillis()))
        val month = curMonth.substring(0, 3)
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        for (i in 0..<mediaList.size) {
            val post = mediaList[i].mediaUrl
            imageUrl = "post_image/${year}/${month}/$post.${System.currentTimeMillis()}"
            post?.let {
                val finalImg = uploadToS3(post)
                postReqList.add(
                    Image(
                        image_thumbnail = finalImg, image_url = finalImg
                    )
                )
            }

        }

    }

    private fun uploadToS3(file: File): String {
        val transferUtility = S3Utils.getTransferUtility(requireActivity())
        val observer: TransferObserver = transferUtility!!.upload(
            AppConstants.BUCKET_NAME, imageUrl, file, CannedAccessControlList.Private
        )
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, newState: TransferState?) {
                if (newState == TransferState.COMPLETED) {
                } else if (newState == TransferState.FAILED) {
                    imageUrl = ""
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

            override fun onError(id: Int, ex: java.lang.Exception?) {
                imageUrl = ""
            }
        })
        return imageUrl
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
                            latitude = latLng.latitude
                            longitude = latLng.longitude
                            try {
                                selectedAddress?.let { address ->
                                    val fullAddress = address.getAddressLine(0).toString()
                                    binding.etAddress.setText(fullAddress)
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

    private fun pickImages() {
        if (selectedImageCount < MAX_IMAGES) {
            val mimeTypes = arrayOf("image/*")
            pickImagesLauncher.launch(mimeTypes)
        } else {
            showToast(requireContext(), "Maximum $MAX_IMAGES images have already been selected")
        }
    }

    private fun cropImage(uri: Uri) {
        val destinationDir = File(requireContext().externalCacheDir, "cropped_images")
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }
        val destinationUri =
            Uri.fromFile(File(destinationDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        val uCrop = UCrop.of(uri, destinationUri)
            .withAspectRatio(16f, 9f) // Set aspect ratio (optional)
        cropImageLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private fun handlePostJobByCompanyResponse() {
        jobsViewModel.postEditJobRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> binding.progressBar.show()
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

    private fun handleJobDetailRes() {
        jobsViewModel.getCompanyPostedJobByIdRes.observe(viewLifecycleOwner) { it ->
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

    @SuppressLint("SetTextI18n")
    private fun setDataOnUi(data: CompanyJobData) = binding.apply {
        data.let {
            postJobSubHead.text = resources.getString(R.string.edit_detail_subtitle)
            btnFollow.text = resources.getString(R.string.save_changes)
            etCompanyName.setText(it.company_name)
            etJobName.setText(it.job_title)
            etWebUrl.setText(it.company_job_url)
            etExperience.setText(it.experience)
            //etExperience.setText(experience)
            etCategory.setText(it.category_name)
            etAddress.setText(it.address)
            etJobDesc.setText(it.description)
            when (it.salary.substringAfter(".").length) {
                2 -> etSalary.setText(it.salary)
                1 -> etSalary.setText(it.salary + "0")
                else -> etSalary.setText(it.salary + ".00")
            }
            if (it.salary.isNotEmpty() && it.salary.contains(".")) {
                when (it.salary.substringAfter(".").length) {
                    2 -> etSalary.setText(it.salary)
                    else -> etSalary.setText(it.salary + "0")
                }
            } else {
                etSalary.setText(it.salary + ".00")
            }
            when (it.job_type) {
                1 -> radioFullTime.isChecked = true
                2 -> radioPartTime.isChecked = true
                3 -> radioProject.isChecked = true
            }
            when (it.age_requirement) {
                0 -> radioUnder18.isChecked = true
                1 -> radioAbove16.isChecked = true
                2 -> radioAbove18.isChecked = true
            }
            if (it.images.isNotEmpty()) {
                for (i in 0 until it.images.size) {
                    val im = S3Utils.generateS3ShareUrl(requireActivity(), it.images[i].imageUrl)
                    val gfgPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(gfgPolicy)
                    val bitmap = getBitmapFromURL(im)
                    val file = bitmap?.let { getImageFile(it) }
                    if (it.images.isNotEmpty()) {
                        mediaList.add(
                            mediaList.size,
                            Medias(
                                mediaUrl = file,
                                mediaThumbnail = file,
                            )
                        )
                    }
                    binding.rvPhotos.show()
                    binding.layoutImage.visibility = View.GONE
                    binding.tvUploadPhoto.visibility = View.GONE
                    mediaAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}