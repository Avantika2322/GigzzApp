package com.gigzz.android.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.common.emitErrorMessage
import com.gigzz.android.common.exceptionHandler
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.data.repositories.ChatRepository
import com.gigzz.android.data.repositories.HomeRepository
import com.gigzz.android.domain.req.CreateChatReq
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.res.AllConnectionRes
import com.gigzz.android.domain.res.CreateChatRes
import com.gigzz.android.domain.res.GetAllChatListRes
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.GetChatMessage
import com.gigzz.android.domain.res.GetChatMessageRes
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
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val pref: PreferenceDataStoreModule
) : ViewModel() {
    var allMessageList: ArrayList<GetChatMessage> = arrayListOf()
    var createChatReq = CreateChatReq()

    private val _getAllChatRes = MutableLiveData<Resource<GetAllChatListRes>>()
    val getAllChatRes: LiveData<Resource<GetAllChatListRes>> = _getAllChatRes

    private val _getAllApplicantChatRes = MutableLiveData<Resource<GetAllChatListRes>>()
    val getAllApplicantChatRes: LiveData<Resource<GetAllChatListRes>> = _getAllApplicantChatRes

    private val _deleteChatRes = MutableLiveData<Resource<GeneralResponse>>()
    val deleteChatRes: LiveData<Resource<GeneralResponse>> = _deleteChatRes

    private val _createChatRes = MutableLiveData<Resource<CreateChatRes>>()
    val createChatRes: LiveData<Resource<CreateChatRes>> = _createChatRes

    private val _createApplicantChatRes = MutableLiveData<Resource<CreateChatRes>>()
    val createApplicantChatRes: LiveData<Resource<CreateChatRes>> = _createApplicantChatRes

    private val _getMessagesRes = MutableLiveData<Resource<GetChatMessageRes>>()
    val getMessagesRes: LiveData<Resource<GetChatMessageRes>> = _getMessagesRes

    private val _getApplicantMessagesRes = MutableLiveData<Resource<GetChatMessageRes>>()
    val getApplicantMessagesRes: LiveData<Resource<GetChatMessageRes>> = _getApplicantMessagesRes


    suspend fun getUserId() =
        pref.appCtx.dataStore.data.first().userId

    suspend fun getUserImg() =
        pref.appCtx.dataStore.data.first().profileThumbnail

    fun getAllChatApi(pageNumber: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllChatRes)) {
                val response = repository.getAllChat(pageNumber)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllChatRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllChatRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllChatRes.postValue(Resource.InternetError())
        }
    }

    fun getAllApplicantChatApi(pageNumber: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllApplicantChatRes)) {
                val response = repository.getAllApplicantChat(pageNumber)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllApplicantChatRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllApplicantChatRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllApplicantChatRes.postValue(Resource.InternetError())
        }
    }

    fun deleteChatApi(chatId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_deleteChatRes)) {
                val response = repository.clearChat(chatId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _deleteChatRes.postValue(Resource.Success(data = result))
                } else {
                    _deleteChatRes.emitErrorMessage(response)
                }
            }
        } else {
            _deleteChatRes.postValue(Resource.InternetError())
        }
    }

    fun createChatApi() {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_createChatRes)) {
                val response = repository.createChat(createChatReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _createChatRes.postValue(Resource.Success(data = result))
                } else {
                    _createChatRes.emitErrorMessage(response)
                }
            }
        } else {
            _createChatRes.postValue(Resource.InternetError())
        }
    }

    fun createApplicantChatApi(chatId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_createApplicantChatRes)) {
                val response = repository.createApplicantChat(createChatReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _createApplicantChatRes.postValue(Resource.Success(data = result))
                } else {
                    _createApplicantChatRes.emitErrorMessage(response)
                }
            }
        } else {
            _createApplicantChatRes.postValue(Resource.InternetError())
        }
    }

    fun getMessagesApi(pageNo:Int,chatId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getMessagesRes)) {
                val response = repository.getMessageById(pageNum = pageNo, chatId = chatId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    if (pageNo == 1) {
                        allMessageList.clear()
                    }
                    result.data?.let { allMessageList.addAll(it) }
                    _getMessagesRes.postValue(Resource.Success(data = result))
                } else {
                    _getMessagesRes.emitErrorMessage(response)
                }
            }
        } else {
            _getMessagesRes.postValue(Resource.InternetError())
        }
    }

    fun getApplicantMessagesApi(pageNo:Int,chatId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getApplicantMessagesRes)) {
                val response = repository.getApplicantMessageById(pageNum = pageNo, chatId = chatId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    if (pageNo == 1) {
                        allMessageList.clear()
                    }
                    result.data?.let { allMessageList.addAll(it) }
                   /* allMessageList.clear()
                    result.data?.let { allMessageList.addAll(it) }*/
                    _getApplicantMessagesRes.postValue(Resource.Success(data = result))
                } else {
                    _getApplicantMessagesRes.emitErrorMessage(response)
                }
            }
        } else {
            _getApplicantMessagesRes.postValue(Resource.InternetError())
        }
    }

    fun clearCreateChatRes(){
        _createChatRes.value=null
        _createApplicantChatRes.value=null
    }

}

