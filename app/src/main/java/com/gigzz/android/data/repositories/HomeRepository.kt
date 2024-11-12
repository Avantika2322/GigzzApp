package com.gigzz.android.data.repositories

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.data.api.ApiClient
import com.gigzz.android.domain.req.AddCommentOnPostReq
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.res.GetAllCommentByIdRes
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.SignInRes
import retrofit2.Response
import javax.inject.Inject

class HomeRepository @Inject constructor(private val apiAuthInterface: ApiAuthInterface) {
   /* suspend fun getFeeds(getAllPostsModel: GetAllPostsModel): SimpleResponse<GetAllPostsRes>? {
        val request = apiClient.getFeedData(getAllPostsModel)
        if (request.failed or !request.isSuccessful) return null
        return request
    }*/

    suspend fun getAllPosts(pageNum: Int, userType: Int): Response<GetAllPostsRes> =
        apiAuthInterface.allPosts(pageNum,userType)

    suspend fun addLikeOnPostsApi(postId: Int): Response<GeneralResponse> =
        apiAuthInterface.addLikeOnPost(postId = postId)

    suspend fun removeLikeFromPostsApi(postId: Int): Response<GeneralResponse> =
        apiAuthInterface.removeLikeFromPost(postId = postId)

    suspend fun sendConnectionReqApi(userId: Int): Response<GeneralResponse> =
        apiAuthInterface.sentConnectionRequest(userId = userId)

    suspend fun cancelConnectionReqApi(userId: Int): Response<GeneralResponse> =
        apiAuthInterface.cancelRequest(userId = userId)

    suspend fun createPostApi(model: CreatePostReq): Response<GeneralResponse> =
        apiAuthInterface.createPost(model = model)

    suspend fun getAllComments(pageNum: Int, postId: Int): Response<GetAllCommentByIdRes> =
        apiAuthInterface.getAllComments(pageNum,postId)

    suspend fun addCommentOnPostApi(commentOnPostReq: AddCommentOnPostReq): Response<GeneralResponse> =
        apiAuthInterface.addCommentOnPost(commentOnPostReq)

    suspend fun editCommentOnPostApi(commentId: Int, comment: String): Response<GeneralResponse> =
        apiAuthInterface.editComment(commentId,comment)

    suspend fun deleteCommentOnPostApi(commentId: Int): Response<GeneralResponse> =
        apiAuthInterface.deleteComment(commentId)
}
