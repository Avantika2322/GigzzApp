package com.gigzz.android.ui.auth.jobseeker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentProfileSetUpBinding
import com.gigzz.android.presentation.AuthViewModel
import com.gigzz.android.ui.home.HomeActivity
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.hide
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class ProfileSetUpFragment : Fragment(R.layout.fragment_profile_set_up) {
    private val binding by viewBinding(FragmentProfileSetUpBinding::bind)
    private var isFromFrag = ""
    private val authViewModel: AuthViewModel by activityViewModels()
    private var imageName: String? = null
    private var filePath: File? = null
    private var month: String? = null
    private var year: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFromFrag= arguments?.getString("from").toString()
        initViews()
        setClickListener()
        uploadMediaObserver()
    }

    private fun initViews() = with(binding) {
        if (isFromFrag == "CompanySignUp") {
            text2.text = getString(R.string.please_upload_logo)
            uploadPicText.text = getString(R.string.upload_logo)
            val underlinedText = SpannableString(uploadPicText.text)
            underlinedText.setSpan(UnderlineSpan(), 0, underlinedText.length, 0)
            uploadPicText.text = underlinedText
        }
    }

    private fun setClickListener() = binding.run {
        navigationIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        uploadPicText.setOnClickListener {
            pickImage()
        }

        skip.setOnClickListener {
            if (isFromFrag == "CompanySignUp") {
                authViewModel.companyReq.emailId = authViewModel.emailId
                authViewModel.signUpAsCompanyApi()
                handleSignUpForCompany()
            } else {
                authViewModel.jobSeeker.emailId = authViewModel.emailId
                authViewModel.signUpAsJobSeekerApi()
                handleJobSeekerSignUp()
            }
        }

        finishBtn.setOnClickListener {
            if (imageName?.isNotEmpty() == true) {
                authViewModel.uploadToS3(filePath!!, imageName!!)
            } else showToast(requireContext(), "Please select a Profile Picture")
        }
    }

    private fun handleSignUpForCompany() {
        authViewModel.signUpAsCompanyResLiveData.observe(viewLifecycleOwner) { response ->
            when(response){
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Success -> {
                    response.data?.data?.let { data ->
                        binding.progressBar.hide()
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        intent.putExtra("userType", data.userType)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), response.message.toString())
                }
                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }
            }
        }
    }

    private fun pickImage() {
        ImagePicker.Companion.with(this.requireActivity())
            .crop()
            .cropOval()
            .maxResultSize(512, 512, true)
            .provider(ImageProvider.BOTH) //Or bothCameraGallery()
            .createIntentFromDialog { launcher.launch(it) }

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                binding.userProfilePic.setImageURI(uri)
                filePath = uri.toFile()
                val formatMonth = SimpleDateFormat("MMMM")
                val curMonth: String = formatMonth.format(Date(System.currentTimeMillis()))
                month = curMonth.substring(0, 3)
                val cal = Calendar.getInstance()
                year = cal[Calendar.YEAR]
                imageName =
                    "profile_image/${year}/${month}/UserProfileImage_${System.currentTimeMillis()}.jpeg"
            }
        }


    private fun uploadMediaObserver() {
        authViewModel.uploadProfilePic.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Success -> {
                    res.data.let {
                        if (isFromFrag == "CompanySignUp") {
                            authViewModel.companyReq.profileThumbnail = it
                            authViewModel.companyReq.profileImageUrl = it
                            authViewModel.companyReq.emailId = authViewModel.emailId
                            authViewModel.signUpAsCompanyApi()
                            handleSignUpForCompany()
                        } else {
                            authViewModel.jobSeeker.profileImageUrl = it
                            authViewModel.jobSeeker.profileThumbnail = it
                            authViewModel.jobSeeker.emailId = authViewModel.emailId
                            authViewModel.signUpAsJobSeekerApi()
                            handleJobSeekerSignUp()
                        }
                    }

                    binding.progressBar.hide()
                }

                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), res.message.toString())
                }

                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }
            }
        }
    }

    private fun handleJobSeekerSignUp() {
        authViewModel.signUpAsJobSeekerResLiveData.observe(viewLifecycleOwner) { response ->
            when(response){
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Success -> {
                    response.data?.data?.let { data ->
                        binding.progressBar.hide()
                        binding.finishBtn.isEnabled = false
                        findNavController().navigate(
                            R.id.action_profileSetUpFragment_to_educationDetailFragment
                        )
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), response.message.toString())
                }
                is Resource.InternetError -> {
                    binding.progressBar.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }
            }
        }
    }
}