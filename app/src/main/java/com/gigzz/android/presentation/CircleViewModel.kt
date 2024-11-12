package com.gigzz.android.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.common.emitErrorMessage
import com.gigzz.android.common.exceptionHandler
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.data.repositories.CircleRepository
import com.gigzz.android.data.repositories.HomeRepository
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.res.AllConnectionRes
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.domain.res.SignInRes
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.dataStore
import com.gigzz.android.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleViewModel @Inject constructor(
    private val circleRepo: CircleRepository,
    private val pref: PreferenceDataStoreModule
) : ViewModel() {
    private val _getAllConnectionRes = MutableLiveData<Resource<AllConnectionRes>>()
    val getAllConnectionRes: LiveData<Resource<AllConnectionRes>> = _getAllConnectionRes

    private val _acceptConnectionRequestRes = MutableLiveData<Resource<GeneralResponse>>()
    private val _deleteConnectionRequestRes = MutableLiveData<Resource<GeneralResponse>>()
    private val _removeConnectionRes = MutableLiveData<Resource<GeneralResponse>>()

    private val _getAllConnectionRequestRes = MutableLiveData<Resource<AllConnectionRes>>()
    val getAllConnectionRequestRes: LiveData<Resource<AllConnectionRes>> = _getAllConnectionRequestRes

    suspend fun getProfileImg() =
        pref.appCtx.dataStore.data.first().profileImageUrl

    fun getMyConnections(pageNumber: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllConnectionRes)) {
                val response = circleRepo.getAllConnections(pageNumber)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllConnectionRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllConnectionRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllConnectionRes.postValue(Resource.InternetError())
        }
    }

    fun getConnectionReq(pageNumber: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllConnectionRequestRes)) {
                val response = circleRepo.getConnectionRequest(pageNumber)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllConnectionRequestRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllConnectionRequestRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllConnectionRequestRes.postValue(Resource.InternetError())
        }
    }

    fun acceptConnectionRequestApi(userId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_acceptConnectionRequestRes)) {
                circleRepo.acceptConnectionRequest(userId = userId)
                getMyConnections(1)
            }
        } else {
            _acceptConnectionRequestRes.postValue(Resource.InternetError())
        }
    }

    fun rejectConnectionRequestApi(userId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_deleteConnectionRequestRes)) {
                circleRepo.rejectConnectionRequest(userId = userId)
            }
        } else {
            _deleteConnectionRequestRes.postValue(Resource.InternetError())
        }
    }

    fun removeConnectionApi(userId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_removeConnectionRes)) {
                circleRepo.removeConnection(userId = userId)
            }
        } else {
            _removeConnectionRes.postValue(Resource.InternetError())
        }
    }
}
