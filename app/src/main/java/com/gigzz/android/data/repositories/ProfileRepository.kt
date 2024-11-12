package com.gigzz.android.data.repositories

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.data.api.ApiClient
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.domain.req.CreateChatReq
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.req.EditProfileReq
import com.gigzz.android.domain.req.EditSkillReq
import com.gigzz.android.domain.res.AllConnectionRes
import com.gigzz.android.domain.res.CreateChatRes
import com.gigzz.android.domain.res.EducationDetailRes
import com.gigzz.android.domain.res.GetAllBlockedUsers
import com.gigzz.android.domain.res.GetAllChatListRes
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.GetAllReviewsRes
import com.gigzz.android.domain.res.GetChatMessageRes
import com.gigzz.android.domain.res.GetJobsRes
import com.gigzz.android.domain.res.GetMasterDataRes
import com.gigzz.android.domain.res.GetProfileRes
import com.gigzz.android.domain.res.RatingReqModel
import com.gigzz.android.domain.res.SignInRes
import retrofit2.Response
import javax.inject.Inject

class ProfileRepository @Inject constructor(private val apiAuthInterface: ApiAuthInterface) {

    suspend fun getMyProfile(): Response<GetProfileRes> =
        apiAuthInterface.getUserProfile()

    suspend fun getMasterData(): Response<GetMasterDataRes> =
        apiAuthInterface.getMasterData()

    suspend fun getUserEducation(): Response<EducationDetailRes> =
        apiAuthInterface.getUserEducationDetails()

    suspend fun addUserEducation(model: AddEditEducationReq): Response<GeneralResponse> =
        apiAuthInterface.addUserEducationDetails(model)

    suspend fun editUserEducation(model: AddEditEducationReq): Response<GeneralResponse> =
        apiAuthInterface.editUserEducationDetails(model)

    suspend fun editUserProfile(model: EditProfileReq): Response<GeneralResponse> =
        apiAuthInterface.editUserProfile(model)

    suspend fun editSkill(model: EditSkillReq): Response<GeneralResponse> =
        apiAuthInterface.editUserSkills(model)

    suspend fun getMyPosts(pageNo:Int): Response<GetAllPostsRes> =
        apiAuthInterface.getUserPosts(pageNo=pageNo)

    suspend fun getOtherUserProfile(userId:Int): Response<GetProfileRes> =
        apiAuthInterface.getOthersProfile(userId)

    suspend fun getOtherUserPosts(pageNo:Int, userId:Int): Response<GetAllPostsRes> =
        apiAuthInterface.getOtherUserPost(pageNo, userId)

    suspend fun getUserRating(): Response<GetAllReviewsRes> =
        apiAuthInterface.getUserRatings()

    suspend fun getOtherUserRatings(userId:Int): Response<GetAllReviewsRes> =
        apiAuthInterface.getOtherUserRatings(userId)

    suspend fun addUserRatings(model: RatingReqModel): Response<GeneralResponse> =
        apiAuthInterface.addRatings(model)

    suspend fun deleteAccount(): Response<GeneralResponse> =
        apiAuthInterface.deleteAccount()

    suspend fun deleteDeviceToken(token: String): Response<GeneralResponse> =
        apiAuthInterface.deleteDeviceToken(token)

    suspend fun unblockUser(userId: Int): Response<GeneralResponse> =
        apiAuthInterface.unBlockUser(userId)

    suspend fun getAllBlockUsers(pageNumber: Int): Response<GetAllBlockedUsers> =
        apiAuthInterface.getAllBlockedUsers(pageNo = pageNumber)

    suspend fun getOtherUserJobsApi(userId: Int): Response<GetJobsRes> =
        apiAuthInterface.getOtherUserJobs(userId)

}
