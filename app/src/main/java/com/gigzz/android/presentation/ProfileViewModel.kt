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
import com.gigzz.android.data.repositories.ProfileRepository
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.domain.req.EditProfileReq
import com.gigzz.android.domain.req.EditSkillReq
import com.gigzz.android.domain.res.EducationDetailRes
import com.gigzz.android.domain.res.EducationDetailsData
import com.gigzz.android.domain.res.GetAllBlockedUsers
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.GetAllReviewsRes
import com.gigzz.android.domain.res.GetJobsRes
import com.gigzz.android.domain.res.GetMasterDataRes
import com.gigzz.android.domain.res.GetProfileRes
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.domain.res.RatingReqModel
import com.gigzz.android.domain.res.ReviewsData
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.dataStore
import com.gigzz.android.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val pref: PreferenceDataStoreModule
) : ViewModel() {
    private var transferUtility: TransferUtility? = null
    var userType: Int? = null
    var educationList= arrayListOf<EducationDetailsData>()
    var postList: ArrayList<PostData> = arrayListOf()
    var reviewList: ArrayList<ReviewsData> = arrayListOf()
    val model = EditProfileReq()

    private val _getMyProfileRes = MutableLiveData<Resource<GetProfileRes>>()
    val getMyProfileRes: LiveData<Resource<GetProfileRes>> = _getMyProfileRes

    private val _getMasterRes = MutableLiveData<Resource<GetMasterDataRes>>()
    val getMasterRes: LiveData<Resource<GetMasterDataRes>> = _getMasterRes

    private val _getEducationRes = MutableLiveData<Resource<EducationDetailRes>>()
    val getEducationRes: LiveData<Resource<EducationDetailRes>> = _getEducationRes

    private val _addEducationRes = MutableLiveData<Resource<GeneralResponse>>()
    val addEducationRes: LiveData<Resource<GeneralResponse>> = _addEducationRes

    private val _editEducationRes = MutableLiveData<Resource<GeneralResponse>>()
    val editEducationRes: LiveData<Resource<GeneralResponse>> = _editEducationRes

    private val _editUserProfileRes = MutableLiveData<Resource<GeneralResponse>>()
    val editUserProfileRes: LiveData<Resource<GeneralResponse>> = _editUserProfileRes

    private val _getUserPostsRes = MutableLiveData<Resource<GetAllPostsRes>>()
    val getUserPostsRes: LiveData<Resource<GetAllPostsRes>> = _getUserPostsRes

    private val _getUserReviewsRes = MutableLiveData<Resource<GetAllReviewsRes>>()
    val getUserReviewsRes: LiveData<Resource<GetAllReviewsRes>> = _getUserReviewsRes

    private val _getAllBlockedUsersRes = MutableLiveData<Resource<GetAllBlockedUsers>>()
    val getAllBlockedUsersRes: LiveData<Resource<GetAllBlockedUsers>> = _getAllBlockedUsersRes

    private val _commonRes = MutableLiveData<Resource<GeneralResponse>>()
    val commonRes: LiveData<Resource<GeneralResponse>> = _commonRes

    private val _getOtherUserJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val getOtherUserJobsRes: LiveData<Resource<GetJobsRes>> = _getOtherUserJobsRes

    private val _uploadProfilePic = MutableLiveData<Resource<String>>()
    val uploadProfilePic: LiveData<Resource<String>> = _uploadProfilePic


    suspend fun getUserDetail() =
        pref.appCtx.dataStore.data.first()

    suspend fun getUserImg() =
        pref.appCtx.dataStore.data.first().profileThumbnail

    init {
        viewModelScope.launch {
            userType= pref.appCtx.dataStore.data.first().userType
        }
    }

    fun getMyProfileApi() {
        if (isNetworkAvailable(pref.appCtx)) {
            _getMyProfileRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getMyProfileRes)) {
                val response = async { repository.getMyProfile()  }
                val eduResponse = async { repository.getUserEducation() }
                val result = response.await()
                val eduResult = eduResponse.await()
                if (result.isSuccessful && result.body() != null && eduResult.isSuccessful && eduResult.body() != null) {
                    educationList.clear()
                    eduResult.body()!!.data?.let { educationList.addAll(it) }
                    _getMyProfileRes.postValue(Resource.Success(data = result.body()!!))
                    _getEducationRes.postValue(Resource.Success(data = eduResult.body()!!))
                } else {
                    _getMyProfileRes.emitErrorMessage(result)
                    _getEducationRes.emitErrorMessage(eduResult)
                }
            }
        } else {
            _getMyProfileRes.postValue(Resource.InternetError())
            _getEducationRes.postValue(Resource.InternetError())
        }
    }

    fun addUserEducationApi(data: AddEditEducationReq) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_addEducationRes)) {
                val response =repository.addUserEducation(data)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getMyProfileApi()
                    _addEducationRes.postValue(Resource.Success(data = result))
                } else {
                    _addEducationRes.emitErrorMessage(response)
                }
            }
        } else {
            _addEducationRes.postValue(Resource.InternetError())
        }
    }

    fun editUserEducationApi(data: AddEditEducationReq) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_editEducationRes)) {
                val response =repository.editUserEducation(data)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getMyProfileApi()
                    _editEducationRes.postValue(Resource.Success(data = result))
                } else {
                    _editEducationRes.emitErrorMessage(response)
                }
            }
        } else {
            _editEducationRes.postValue(Resource.InternetError())
        }
    }

    fun editProfileApi(data: EditProfileReq) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_editUserProfileRes)) {
                val response =repository.editUserProfile(data)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getMyProfileApi()
                    updateUserData(data)
                    _editUserProfileRes.postValue(Resource.Success(data = result))
                } else {
                    _editUserProfileRes.emitErrorMessage(response)
                }
            }
        } else {
            _editUserProfileRes.postValue(Resource.InternetError())
        }
    }



    fun editSkillApi(data: EditSkillReq) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_editUserProfileRes)) {
                val response =repository.editSkill(data)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getMyProfileApi()
                    _editUserProfileRes.postValue(Resource.Success(data = result))
                } else {
                    _editUserProfileRes.emitErrorMessage(response)
                }
            }
        } else {
            _editUserProfileRes.postValue(Resource.InternetError())
        }
    }

    fun getMasterDataApi() {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getMasterRes)) {
                val response =repository.getMasterData()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getMasterRes.postValue(Resource.Success(data = result))
                } else {
                    _getMasterRes.emitErrorMessage(response)
                }
            }
        } else {
            _getMasterRes.postValue(Resource.InternetError())
        }
    }

    fun getOtherUserProfileApi(userId:Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            _getMyProfileRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getMyProfileRes)) {
                val response =repository.getOtherUserProfile(userId = userId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getMyProfileRes.postValue(Resource.Success(data = result))
                } else {
                    _getMyProfileRes.emitErrorMessage(response)
                }
            }
        } else {
            _getMyProfileRes.postValue(Resource.InternetError())
        }
    }

    fun getOtherUserJobsApi(userId:Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            _getOtherUserJobsRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getOtherUserJobsRes)) {
                val response =repository.getOtherUserJobsApi(userId = userId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getOtherUserJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getOtherUserJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getOtherUserJobsRes.postValue(Resource.InternetError())
        }
    }

    fun getMyPostsApi(pageNo: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            _getUserPostsRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getUserPostsRes)) {
                val response =repository.getMyPosts(pageNo = pageNo)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getUserPostsRes.postValue(Resource.Success(data = result))
                } else {
                    _getUserPostsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getUserPostsRes.postValue(Resource.InternetError())
        }
    }

    fun getOtherUserPostsApi(pageNo: Int,userId:Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            _getUserPostsRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getUserPostsRes)) {
                val response =repository.getOtherUserPosts(pageNo = pageNo, userId = userId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getUserPostsRes.postValue(Resource.Success(data = result))
                } else {
                    _getUserPostsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getUserPostsRes.postValue(Resource.InternetError())
        }
    }

    fun getMyReviewsApi() {
        if (isNetworkAvailable(pref.appCtx)) {
            _getUserReviewsRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getUserReviewsRes)) {
                val response =repository.getUserRating()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getUserReviewsRes.postValue(Resource.Success(data = result))
                } else {
                    _getUserReviewsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getUserReviewsRes.postValue(Resource.InternetError())
        }
    }

    fun getOtherUserReviewsApi(userId:Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            _getUserReviewsRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getUserReviewsRes)) {
                val response =repository.getOtherUserRatings(userId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getUserReviewsRes.postValue(Resource.Success(data = result))
                } else {
                    _getUserReviewsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getUserReviewsRes.postValue(Resource.InternetError())
        }
    }

    fun addReviewsApi(model: RatingReqModel) {
        if (isNetworkAvailable(pref.appCtx)) {
            _commonRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_commonRes)) {
                val response =repository.addUserRatings(model)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _commonRes.postValue(Resource.Success(data = result))
                } else {
                    _commonRes.emitErrorMessage(response)
                }
            }
        } else {
            _commonRes.postValue(Resource.InternetError())
        }
    }

    fun getAllBlockedUsersApi(pageNo: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            _getAllBlockedUsersRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_getAllBlockedUsersRes)) {
                val response =repository.getAllBlockUsers(pageNo)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllBlockedUsersRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllBlockedUsersRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllBlockedUsersRes.postValue(Resource.InternetError())
        }
    }

    fun deleteAccountApi() {
        if (isNetworkAvailable(pref.appCtx)) {
            _commonRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_commonRes)) {
                val response =repository.deleteAccount()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    clearUserData()
                    _commonRes.postValue(Resource.Success(data = result))
                } else {
                    _commonRes.emitErrorMessage(response)
                }
            }
        } else {
            _commonRes.postValue(Resource.InternetError())
        }
    }

    fun deleteTokenApi() {
        if (isNetworkAvailable(pref.appCtx)) {
            _commonRes.postValue(Resource.Loading())
            viewModelScope.launch(exceptionHandler(_commonRes)) {
                val response =repository.deleteDeviceToken(pref.getFirstPreference(PrefKeys.DEVICE_TOKEN,""))
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    clearUserData()
                    _commonRes.postValue(Resource.Success(data = result))
                } else {
                    _commonRes.emitErrorMessage(response)
                }
            }
        } else {
            _commonRes.postValue(Resource.InternetError())
        }
    }

    private suspend fun clearUserData() {
        pref.putPreference(PrefKeys.AUTH_KEY, "")
        pref.putPreference(PrefKeys.DEVICE_TOKEN, "")
        pref.appCtx.dataStore.updateData {
            it.copy(
                address = "",
                ageGroup = 0,
                authKey = "",
                bio = "",
                countryCode = "",
                dob = "",
                emailId = "",
                fullName = "",
                gender = "",
                interest = arrayListOf(),
                lat = 0.0,
                lon = 0.0,
                parentEmailId = "",
                phoneNumber = "",
                profileImageUrl = "",
                profileThumbnail = "",
                resumeUrl = "",
                skills = arrayListOf(),
                totalConnection = 0,
                totalReviewRating = 0,
                userType = -1,
                workPermitUrl = "",
                zipCode = "",
                averageRating = 0,
                userId = 0,
                companyName = ""
            )
        }
    }

    fun uploadToS3(file: File, fileName: String) {
        if (isNetworkAvailable(pref.appCtx)) {
            transferUtility = S3Utils.getTransferUtility(pref.appCtx)
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

    private suspend fun updateUserData(data: EditProfileReq) {
        pref.appCtx.dataStore.updateData {
            it.copy(
                address = data.address,
                bio = data.bio,
                countryCode = data.countryCode,
                lat = data.lat?.toDouble(),
                lon = data.lon?.toDouble(),
                phoneNumber = data.phoneNumber,
                profileImageUrl = data.profileImage,
                profileThumbnail = data.profileImage,
                zipCode = data.zipCode
            )
        }
    }

    fun clearEditAddEduRes(){
        _editEducationRes.value=null
        _addEducationRes.value=null
        _editUserProfileRes.value=null
    }

}

