package com.gigzz.android.data.repositories

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.data.api.ApiClient
import com.gigzz.android.domain.req.CreateChatReq
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.res.AllConnectionRes
import com.gigzz.android.domain.res.CreateChatRes
import com.gigzz.android.domain.res.GetAllChatListRes
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.GetChatMessageRes
import com.gigzz.android.domain.res.SignInRes
import retrofit2.Response
import javax.inject.Inject

class ChatRepository @Inject constructor(private val apiAuthInterface: ApiAuthInterface) {

    suspend fun getAllChat(pageNum: Int): Response<GetAllChatListRes> =
        apiAuthInterface.getAllChats(pageNum)

    suspend fun getAllApplicantChat(pageNum: Int): Response<GetAllChatListRes> =
        apiAuthInterface.getAllApplicantsChat(pageNum)

    suspend fun clearChat(chatId: Int): Response<GeneralResponse> =
        apiAuthInterface.clearChats(chatId)

    suspend fun getMessageById(chatId: Int,pageNum: Int): Response<GetChatMessageRes> =
        apiAuthInterface.getMessageById(chatId,pageNum)

    suspend fun getApplicantMessageById(chatId: Int,pageNum: Int): Response<GetChatMessageRes> =
        apiAuthInterface.getApplicantMessageById(chatId,pageNum)

    suspend fun createChat(model: CreateChatReq): Response<CreateChatRes> =
        apiAuthInterface.createChat(model)

    suspend fun createApplicantChat(model: CreateChatReq): Response<CreateChatRes> =
        apiAuthInterface.createChat(model)
}
