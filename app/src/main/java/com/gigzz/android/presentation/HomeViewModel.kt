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
import com.gigzz.android.data.repositories.HomeRepository
import com.gigzz.android.domain.req.AddCommentOnPostReq
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.res.GetAllCommentByIdRes
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
class HomeViewModel @Inject constructor(
    private val homeRepo: HomeRepository,
    private val pref: PreferenceDataStoreModule
) : ViewModel() {
    var postList: ArrayList<PostData> = arrayListOf()
    var userType = 0
    var userId=-1
    private val _getAllPostRes = MutableLiveData<Resource<GetAllPostsRes>>()
    val getAllPostRes: LiveData<Resource<GetAllPostsRes>> = _getAllPostRes

    private val _getAllCommentRes = MutableLiveData<Resource<GetAllCommentByIdRes>>()
    val getAllCommentRes: LiveData<Resource<GetAllCommentByIdRes>> = _getAllCommentRes

    private val _likeDislikeResponse = MutableLiveData<Resource<GeneralResponse>>()

    private val _createPostResponse = MutableLiveData<Resource<GeneralResponse>>()
    val createPostResponse: LiveData<Resource<GeneralResponse>> = _createPostResponse

    private val _connectionResponse = MutableLiveData<Resource<GeneralResponse>>()

    private val _commonResponse = MutableLiveData<Resource<GeneralResponse>>()
    val commonResponse: LiveData<Resource<GeneralResponse>> = _commonResponse

    init {
        viewModelScope.launch {
            userId=pref.appCtx.dataStore.data.first().userId!!
        }
    }
    suspend fun getProfileRes() =
        pref.appCtx.dataStore.data.first()

    fun getAllPosts(pageNumber: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllPostRes)) {
                val response = homeRepo.getAllPosts(pageNumber, userType)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllPostRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllPostRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllPostRes.postValue(Resource.InternetError())
        }
    }

    fun likePostApi(postId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_likeDislikeResponse)) {
                homeRepo.addLikeOnPostsApi(postId = postId)
            }
        } else {
            _likeDislikeResponse.postValue(Resource.InternetError())
        }
    }

    fun removeLikeApi(postId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_likeDislikeResponse)) {
                homeRepo.removeLikeFromPostsApi(postId = postId)
            }
        } else {
            _likeDislikeResponse.postValue(Resource.InternetError())
        }
    }

    fun sendConnectionApi(userId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_connectionResponse)) {
                homeRepo.sendConnectionReqApi(userId = userId)
            }
        } else {
            _connectionResponse.postValue(Resource.InternetError())
        }
    }

    fun cancelConnectionApi(userId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_connectionResponse)) {
                homeRepo.cancelConnectionReqApi(userId = userId)
            }
        } else {
            _connectionResponse.postValue(Resource.InternetError())
        }
    }

    fun likePost(postId: Int, pos: Int) {
        likePostApi(postId)
        for ((index, i) in postList.withIndex()) {
            if (i.postId == postId && index != pos) {
                postList[index] =
                    i.copy(isLiked = 1, totalLikesCount = i.totalLikesCount?.plus(1))
                break
            }
        }
    }

    fun removeLikePost(postId: Int, pos: Int) {
        removeLikeApi(postId)
        for ((index, i) in postList.withIndex()) {
            if (i.postId == postId && index != pos) {
                postList[index] = i.copy(
                    isLiked = 0,
                    totalLikesCount = if (i.totalLikesCount!! > 0) (i.totalLikesCount?.minus(1)) else 0
                )
                break
            }
        }
    }

    fun createPost(model: CreatePostReq) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_createPostResponse)) {
                val response = homeRepo.createPostApi(model)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getAllPosts(1)
                    _createPostResponse.postValue(Resource.Success(data = result))
                } else {
                    _createPostResponse.emitErrorMessage(response)
                }
            }
        } else {
            _createPostResponse.postValue(Resource.InternetError())
        }
    }

    fun getAllComments(pageNumber: Int,postId: Int) {
        _getAllCommentRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllCommentRes)) {
                val response = homeRepo.getAllComments(pageNumber, postId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllCommentRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllCommentRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllCommentRes.postValue(Resource.InternetError())
        }
    }

    fun addCommentOnPostApi(postId: Int, comment: String) {
        _commonResponse.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_commonResponse)) {
                val response = homeRepo.addCommentOnPostApi(AddCommentOnPostReq(postId = postId.toString(), comment = comment))
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getAllComments(1,postId)
                    _commonResponse.postValue(Resource.Success(data = result))
                } else {
                    _commonResponse.emitErrorMessage(response)
                }
            }
        } else {
            _commonResponse.postValue(Resource.InternetError())
        }
    }

    fun editCommentOnPostApi(postId: Int,commentId: Int, comment: String) {
        _commonResponse.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_commonResponse)) {
                val response = homeRepo.editCommentOnPostApi(commentId = commentId, comment = comment)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getAllComments(1,postId)
                    _commonResponse.postValue(Resource.Success(data = result))
                } else {
                    _commonResponse.emitErrorMessage(response)
                }
            }
        } else {
            _commonResponse.postValue(Resource.InternetError())
        }
    }

    fun deleteCommentOnPostApi(commentId: Int) {
        _commonResponse.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_commonResponse)) {
                val response = homeRepo.deleteCommentOnPostApi(commentId = commentId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _commonResponse.postValue(Resource.Success(data = result))
                } else {
                    _commonResponse.emitErrorMessage(response)
                }
            }
        } else {
            _commonResponse.postValue(Resource.InternetError())
        }
    }

    fun clearCreatePostResponse(){
        _createPostResponse.value=null
        _commonResponse.value=null
    }

    fun clearCommentResponse(){
        _getAllCommentRes.value=null
    }
}
