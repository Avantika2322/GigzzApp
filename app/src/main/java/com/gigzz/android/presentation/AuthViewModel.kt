package com.gigzz.android.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.common.emitErrorMessage
import com.gigzz.android.common.exceptionHandler
import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.data.repositories.AuthRepository
import com.gigzz.android.domain.Demo
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.domain.req.DeviceInfo
import com.gigzz.android.domain.req.SignUpAsCompanyReq
import com.gigzz.android.domain.req.SignUpAsIndividualJobGiverReq
import com.gigzz.android.domain.req.SignUpAsJobSeeker
import com.gigzz.android.domain.res.GetMasterDataRes
import com.gigzz.android.domain.res.MasterData
import com.gigzz.android.domain.res.SendOtpRes
import com.gigzz.android.domain.res.SignInRes
import com.gigzz.android.ui.auth.individual.IndividualJobGiverProfileFragment
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.DeviceInfoModule
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.dataStore
import com.gigzz.android.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preference: PreferenceDataStoreModule,
    private val deviceInfoModule: DeviceInfoModule,
    private val authRepository: AuthRepository
) : ViewModel() {
    var emailId: String = ""
    val jobSeeker = SignUpAsJobSeeker()
    val companyReq = SignUpAsCompanyReq()
    private var transferUtility: TransferUtility? = null
    val individualJobGiverReq = SignUpAsIndividualJobGiverReq()
    private val _deviceInfoLiveData = MutableLiveData<DeviceInfo?>()

    private val _uploadProfilePic = MutableLiveData<Resource<String>>()
    val uploadProfilePic: LiveData<Resource<String>> = _uploadProfilePic

    private val _demoApi = MutableLiveData<Resource<Demo>>()
    val demoApi: LiveData<Resource<Demo>> = _demoApi

    private val _signInResLiveData = MutableLiveData<Resource<SignInRes>>()
    val signInResLiveData: LiveData<Resource<SignInRes>> = _signInResLiveData

    private val _sendOtpResLiveData = MutableLiveData<Resource<SendOtpRes>>()
    val sendOtpResLiveData: LiveData<Resource<SendOtpRes>> = _sendOtpResLiveData

    private val _verifyOtpResLiveData = MutableLiveData<Resource<GeneralResponse>>()
    val verifyOtpResLiveData: LiveData<Resource<GeneralResponse>> = _verifyOtpResLiveData

    private val _masterDataResLiveData = MutableLiveData<Resource<GetMasterDataRes>>()
    val masterDataResLiveData: LiveData<Resource<GetMasterDataRes>> = _masterDataResLiveData

    private val _signUpAsJobSeekerResLiveData = MutableLiveData<Resource<SignInRes>>()
    val signUpAsJobSeekerResLiveData: LiveData<Resource<SignInRes>> = _signUpAsJobSeekerResLiveData

    private val _signUpAsCompanyResLiveData = MutableLiveData<Resource<SignInRes>>()
    val signUpAsCompanyResLiveData: LiveData<Resource<SignInRes>> = _signUpAsCompanyResLiveData

    private val _signUpAsIndividualResLiveData = MutableLiveData<Resource<SignInRes>>()
    val signUpAsIndividualResLiveData: LiveData<Resource<SignInRes>> = _signUpAsIndividualResLiveData

    private val _updateDeviceInfoRes = MutableLiveData<Resource<GeneralResponse>>()

    init {
        viewModelScope.launch {
            deviceInfoModule.getDeviceInfo(preference).collectLatest {
                when (it) {
                    is Resource.Error -> {}
                    is Resource.InternetError -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _deviceInfoLiveData.postValue(it.data)
                    }
                }
            }
        }
    }

    fun sendOtpToUserApi(emailId: String){
        if (isNetworkAvailable(preference.appCtx)) {
            _sendOtpResLiveData.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_sendOtpResLiveData)) {
                val response = authRepository.sendOtpApi(emailId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _sendOtpResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _sendOtpResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _sendOtpResLiveData.postValue(Resource.InternetError())
        }
    }

    fun verifyOtpApi(emailId: String, otp: String){
        if (isNetworkAvailable(preference.appCtx)) {
            _verifyOtpResLiveData.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_verifyOtpResLiveData)) {
                val response = authRepository.verifyOtpApi(emailId,otp)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _verifyOtpResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _verifyOtpResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _verifyOtpResLiveData.postValue(Resource.InternetError())
        }
    }

    fun demoApi(){
        if (isNetworkAvailable(preference.appCtx)) {
            _demoApi.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_demoApi)) {
                val response = authRepository.demoDataApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _demoApi.postValue(Resource.Success(data = result))
                } else {
                    _demoApi.emitErrorMessage(response)
                }
            }
        } else {
            _demoApi.postValue(Resource.InternetError())
        }
    }

    fun getMasterDataApi(){
        if (isNetworkAvailable(preference.appCtx)) {
            _masterDataResLiveData.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_masterDataResLiveData)) {
                val response = authRepository.getMasterDataApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _masterDataResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _masterDataResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _masterDataResLiveData.postValue(Resource.InternetError())
        }
    }

    fun signUpAsJobSeekerApi(){
        if (isNetworkAvailable(preference.appCtx)) {
            _signUpAsJobSeekerResLiveData.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_signUpAsJobSeekerResLiveData)) {
                val response = authRepository.signUpAsJobSeekerApi(jobSeeker)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    saveUserData(result)
                    updateDeviceInfo()
                    _signUpAsJobSeekerResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _signUpAsJobSeekerResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _signUpAsJobSeekerResLiveData.postValue(Resource.InternetError())
        }
    }

    fun signUpAsCompanyApi(){
        if (isNetworkAvailable(preference.appCtx)) {
            _signUpAsCompanyResLiveData.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_signUpAsCompanyResLiveData)) {
                val response = authRepository.signUpAsCompanyApi(companyReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    saveUserData(result)
                    updateDeviceInfo()
                    _signUpAsCompanyResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _signUpAsCompanyResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _signUpAsCompanyResLiveData.postValue(Resource.InternetError())
        }
    }

    fun signUpAsIndividualApi(){
        if (isNetworkAvailable(preference.appCtx)) {
            _signUpAsIndividualResLiveData.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_signUpAsIndividualResLiveData)) {
                val response = authRepository.signUpAsIndividualApi(individualJobGiverReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    saveUserData(result)
                    updateDeviceInfo()
                    _signUpAsIndividualResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _signUpAsIndividualResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _signUpAsIndividualResLiveData.postValue(Resource.InternetError())
        }
    }

    fun signInUser(emailId: String, password: String){
        if (isNetworkAvailable(preference.appCtx)) {
            viewModelScope.launch(exceptionHandler(_signInResLiveData)) {
                val response = authRepository.signInApi(emailId,password)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    saveUserData(result)
                    updateDeviceInfo()
                    _signInResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _signInResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _signInResLiveData.postValue(Resource.InternetError())
        }
    }

    private fun updateDeviceInfo(){
        if (isNetworkAvailable(preference.appCtx)) {
            viewModelScope.launch(exceptionHandler(_updateDeviceInfoRes)) {
                val response = _deviceInfoLiveData.value?.let {
                    authRepository.updateDeviceInfoApi(
                        it
                    )
                }
                val result = response?.body()
                if (response!!.isSuccessful && result != null) {
                    _updateDeviceInfoRes.postValue(Resource.Success(data = result))
                } else {
                    _updateDeviceInfoRes.emitErrorMessage(response)
                }
            }
        } else {
            _updateDeviceInfoRes.postValue(Resource.InternetError())
        }
    }

    fun editAddEducationApi(addEditEducationReq: AddEditEducationReq){
        if (isNetworkAvailable(preference.appCtx)) {
            viewModelScope.launch(exceptionHandler(_verifyOtpResLiveData)) {
                val response = authRepository.addEditEducationApi(addEditEducationReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _verifyOtpResLiveData.postValue(Resource.Success(data = result))
                } else {
                    _verifyOtpResLiveData.emitErrorMessage(response)
                }
            }
        } else {
            _verifyOtpResLiveData.postValue(Resource.InternetError())
        }
    }

    private suspend fun saveUserData(data: SignInRes) {
        preference.putPreference(PrefKeys.AUTH_KEY, data.data?.authKey!!)
        preference.appCtx.dataStore.updateData {
            it.copy(
                address = data.data.address,
                ageGroup = data.data.ageGroup,
                authKey = data.data.authKey,
                bio = data.data.bio,
                countryCode = data.data.countryCode,
                dob = data.data.dob,
                emailId = data.data.emailId,
                fullName = data.data.fullName,
                gender = data.data.gender,
                interest = data.data.interest,
                lat = data.data.lat,
                lon = data.data.lon,
                parentEmailId = data.data.parentEmailId,
                phoneNumber = data.data.phoneNumber,
                profileImageUrl = data.data.profileImageUrl,
                profileThumbnail = data.data.profileThumbnail,
                resumeUrl = data.data.resumeUrl,
                skills = data.data.skills,
                totalConnection = data.data.totalConnection,
                totalReviewRating = data.data.totalReviewRating,
                userType = data.data.userType,
                workPermitUrl = data.data.workPermitUrl,
                zipCode = data.data.zipCode,
                averageRating = data.data.averageRating,
                userId = data.data.userId,
                companyName = data.data.companyName
            )
        }
    }

    fun uploadToS3(file: File,fileName: String) {
        if (isNetworkAvailable(preference.appCtx)) {
            transferUtility = S3Utils.getTransferUtility(preference.appCtx)
            val observer: TransferObserver? = transferUtility?.upload(
                AppConstants.BUCKET_NAME,
                fileName,
                file,
                CannedAccessControlList.Private
            )
            _uploadProfilePic.postValue(Resource.Loading(null))
            observer?.setTransferListener(UploadListener(fileName))
        } else {
            _uploadProfilePic.postValue(Resource.InternetError())
        }
    }

    inner class UploadListener(
        private val fileName: String
    ) : TransferListener {

        override fun onError(id: Int, e: Exception) {
            Log.e("TAG", "Error during upload: $id", e)
            _uploadProfilePic.postValue(Resource.Error(e.toString()))
        }

        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            Log.d(
                "TAG", String.format(
                    "onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent
                )
            )
        }

        override fun onStateChanged(id: Int, newState: TransferState) {
            Log.d("TAG", "onStateChanged: $id, $newState")

            if (newState == TransferState.COMPLETED) {
                _uploadProfilePic.postValue(Resource.Success(fileName))

            } else if (newState == TransferState.FAILED) {
                _uploadProfilePic.postValue(Resource.Error(""))
            }
        }
    }

    fun clearRes(){
        _sendOtpResLiveData.value= null
        _verifyOtpResLiveData.value= null
    }
}