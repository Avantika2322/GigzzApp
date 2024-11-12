package com.gigzz.android.data.repositories

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.data.api.ApiClient
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.res.AllConnectionRes
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.SignInRes
import retrofit2.Response
import javax.inject.Inject

class CircleRepository @Inject constructor(private val apiAuthInterface: ApiAuthInterface) {

    suspend fun getAllConnections(pageNum: Int): Response<AllConnectionRes> =
        apiAuthInterface.getMyConnections(pageNum)

    suspend fun getConnectionRequest(pageNum: Int): Response<AllConnectionRes> =
        apiAuthInterface.getConnectionRequests(pageNum)

    suspend fun acceptConnectionRequest(userId: Int): Response<GeneralResponse> =
        apiAuthInterface.acceptRequest(userId)

    suspend fun rejectConnectionRequest(userId: Int): Response<GeneralResponse> =
        apiAuthInterface.rejectRequest(userId)

    suspend fun removeConnection(userId: Int): Response<GeneralResponse> =
        apiAuthInterface.removeUserConnection(userId)
}
