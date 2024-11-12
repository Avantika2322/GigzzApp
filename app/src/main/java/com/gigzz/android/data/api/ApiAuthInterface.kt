package com.gigzz.android.data.api

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.domain.req.AddCommentOnPostReq
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.domain.req.AllJobsFilterReq
import com.gigzz.android.domain.req.CompanyJobsFilterReq
import com.gigzz.android.domain.req.CreateChatReq
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.req.DeviceInfo
import com.gigzz.android.domain.req.EditProfileReq
import com.gigzz.android.domain.req.EditSkillReq
import com.gigzz.android.domain.req.IndividualJobsFilterReq
import com.gigzz.android.domain.req.PostNewJobByCompany
import com.gigzz.android.domain.req.PostNewJobByIndividualReq
import com.gigzz.android.domain.res.AllConnectionRes
import com.gigzz.android.domain.res.CreateChatRes
import com.gigzz.android.domain.res.EducationDetailRes
import com.gigzz.android.domain.res.GetAllBlockedUsers
import com.gigzz.android.domain.res.GetAllChatListRes
import com.gigzz.android.domain.res.GetAllCommentByIdRes
import com.gigzz.android.domain.res.GetAllCompanyPostedJobsRes
import com.gigzz.android.domain.res.GetAllPostsRes
import com.gigzz.android.domain.res.GetAllReviewsRes
import com.gigzz.android.domain.res.GetChatMessageRes
import com.gigzz.android.domain.res.GetJobsRes
import com.gigzz.android.domain.res.GetMasterDataRes
import com.gigzz.android.domain.res.GetCompanyPostedJobByIdRes
import com.gigzz.android.domain.res.GetPostedJobByIdRes
import com.gigzz.android.domain.res.GetProfileRes
import com.gigzz.android.domain.res.RatingReqModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiAuthInterface {

    @POST("update-device-token")
    suspend fun updateDeviceToken(@Body value: DeviceInfo): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("get-all-posts")
    suspend fun allPosts(@Field("page_no") pageNo: Int, @Field("user_type") userType: Int): Response<GetAllPostsRes>

    @FormUrlEncoded
    @POST("like-post")
    suspend fun addLikeOnPost(
        @Field("post_id") postId: Int
    ): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("dislike-post")
    suspend fun removeLikeFromPost(
        @Field("post_id") postId: Int
    ): Response<GeneralResponse>

    @GET("get-masters-data")
    suspend fun getMasterData(): Response<GetMasterDataRes>

    @FormUrlEncoded
    @POST("accept-connection-request")
    suspend fun acceptRequest(@Field("other_user_id") userId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("reject-connection-request")
    suspend fun rejectRequest(@Field("other_user_id") userId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("sent-connection-request")
    suspend fun sentConnectionRequest(@Field("other_user_id") userId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("remove-user-connection")
    suspend fun removeUserConnection(@Field("other_user_id") userId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("cancel-connection-request")
    suspend fun cancelRequest(@Field("other_user_id") userId: Int): Response<GeneralResponse>

    @POST("create-post")
    suspend fun createPost(@Body model: CreatePostReq): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("get-post-comments-by-post-id")
    suspend fun getAllComments(@Field("page_no") pageNo: Int, @Field("post_id") postId: Int): Response<GetAllCommentByIdRes>

    @POST("comment-on-post")
    suspend fun addCommentOnPost(@Body model: AddCommentOnPostReq): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("delete-comment-from-post")
    suspend fun deleteComment(@Field("comment_id") commentId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("report-comment")
    suspend fun reportComment(@Field("comment_id") commentId: Int, @Field("flagged_type_id") flaggedId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("edit-posts-comments")
    suspend fun editComment(@Field("comment_id") commentId: Int, @Field("comment") comment: String): Response<GeneralResponse>


    //circle
    @FormUrlEncoded
    @POST("get-all-connection")
    suspend fun getMyConnections(@Field("page_no") pageNo: Int): Response<AllConnectionRes>

    @FormUrlEncoded
    @POST("get-all-connection-request")
    suspend fun getConnectionRequests(@Field("page_no") pageNo: Int): Response<AllConnectionRes>

    //chat
    @FormUrlEncoded
    @POST("get-all-chats")
    suspend fun getAllChats(@Field("page_no") pageNo: Int): Response<GetAllChatListRes>

    @FormUrlEncoded
    @POST("get-all-applicants-chats")
    suspend fun getAllApplicantsChat(@Field("page_no") pageNo: Int): Response<GetAllChatListRes>

    @FormUrlEncoded
    @POST("delete-chat-messages")
    suspend fun clearChats(@Field("chat_id") chatId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("get-messages-by-chat-id")
    suspend fun getMessageById(@Field("chat_id") chatId: Int,@Field("page_no") pageNo: Int): Response<GetChatMessageRes>

    @POST("create-chat")
    suspend fun createChat(@Body chatByIdModel: CreateChatReq): Response<CreateChatRes>

    @POST("create-applicants-chats")
    suspend fun createApplicantsChat(@Body chatByIdModel: CreateChatReq): Response<CreateChatRes>

    @FormUrlEncoded
    @POST("get-applicants-messages-by-chat-id")
    suspend fun getApplicantMessageById(@Field("chat_id") chatId: Int,@Field("page_no") pageNo: Int): Response<GetChatMessageRes>

    //profile
    @POST("get-my-profile")
    suspend fun getUserProfile(): Response<GetProfileRes>

    @POST("edit-condidate-skills")
    suspend fun editUserSkills(@Body model: EditSkillReq): Response<GeneralResponse>

    @POST("edit-profile")
    suspend fun editUserProfile(@Body model: EditProfileReq): Response<GeneralResponse>

    @POST("edit-user-education-stage")
    suspend fun editUserEducationDetails(@Body model: AddEditEducationReq): Response<GeneralResponse>

    @POST("delete-user-education-stage")
    suspend fun deleteUserEducationDetails(@Body model: AddEditEducationReq): Response<GeneralResponse>

    @POST("add-user-education-stage")
    suspend fun addUserEducationDetails(@Body model: AddEditEducationReq): Response<GeneralResponse>

    @POST("get-all-education-stage")
    suspend fun getUserEducationDetails(): Response<EducationDetailRes>

    /*@POST("get-other-user-education-stage")
    suspend fun getOtherUserEducationDetails(@Body model: RatingReqModel): Response<EducationDetailsRes>
*/

    @FormUrlEncoded
    @POST("get-other-user-profile")
    suspend fun getOthersProfile(@Field("other_user_id") otherUserId:Int): Response<GetProfileRes>

    @FormUrlEncoded
    @POST("get-my-posts")
    suspend fun getUserPosts(@Field("page_no") pageNo: Int): Response<GetAllPostsRes>

    @FormUrlEncoded
    @POST("get-other-user-posts")
    suspend fun getOtherUserPost(@Field("page_no") pageNo: Int, @Field("other_user_id") otherUserId:Int): Response<GetAllPostsRes>

    @POST("get-user-rating")
    suspend fun getUserRatings(): Response<GetAllReviewsRes>

    @FormUrlEncoded
    @POST("get-other-user-jobs")
    suspend fun getOtherUserJobs(@Field("other_user_id") userId: Int): Response<GetJobsRes>

    @FormUrlEncoded
    @POST("get-other-user-rating")
    suspend fun getOtherUserRatings(@Field("other_user_id") otherUserId:Int): Response<GetAllReviewsRes>

    @POST("user-rating")
    suspend fun addRatings(@Body model: RatingReqModel): Response<GeneralResponse>

    @POST("delete-account")
    suspend fun deleteAccount(): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("delete-device-token")
    suspend fun deleteDeviceToken(@Field("device_token") token: String): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("get-all-blocked-user")
    suspend fun getAllBlockedUsers(@Field("page_no") pageNo: Int): Response<GetAllBlockedUsers>

    @FormUrlEncoded
    @POST("unblock-user")
    suspend fun unBlockUser(@Field("unblock_to_user_id") userId: Int): Response<GeneralResponse>

    //jobs
    @FormUrlEncoded
    @POST("get-posted-job")
    suspend fun getAllJobs(@Field("job_type") jobType:Int, @Field("page_no") pageNo: Int): Response<GetJobsRes>

    @GET("get-bookmarked-jobs")
    suspend fun getBookmarkedJobs(): Response<GetJobsRes>

    @POST("get-my-applied-jobs")
    suspend fun getAppliedJobs(): Response<GetJobsRes>

    @POST("get-my-jobs")
    suspend fun getAllIndividualJobs(): Response<GetJobsRes>

    @POST("get-my-jobs")
    suspend fun getAllCompanyJobs(): Response<GetAllCompanyPostedJobsRes>

    @POST("individual-jobs-filter")
    suspend fun individualJobFilter(@Body model: IndividualJobsFilterReq): Response<GetJobsRes>

    @POST("company-jobs-filter")
    suspend fun companyJobFilter(@Body model: CompanyJobsFilterReq): Response<GetAllCompanyPostedJobsRes>

    @FormUrlEncoded
    @POST("get-my-applied-job-filter")
    suspend fun getMyAppliedJobFilter(@Field("search_status") searchStatus: Int): Response<GetJobsRes>

    @POST("job-seeker-posted-job-filter")
    suspend fun allJobsFilter(@Body model: AllJobsFilterReq): Response<GetJobsRes>

    @FormUrlEncoded
    @POST("get-posted-job")
    suspend fun getPostedJobs(@Field("job_type") jobType:Int, @Field("page_no") pageNo: Int): Response<GetJobsRes>

    @FormUrlEncoded
    @POST("get-posted-job-by-id")
    suspend fun getPostedJobById(@Field("job_id") jobId: Int): Response<GetPostedJobByIdRes>

    @FormUrlEncoded
    @POST("get-posted-job-by-id")
    suspend fun getCompanyPostedJobById(@Field("job_id") jobId: Int): Response<GetCompanyPostedJobByIdRes>

    @FormUrlEncoded
    @POST("bookmark-jobs")
    suspend fun bookmarkJob(@Field("job_id") jobId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("remove-bookmarked-jobs")
    suspend fun removeBookmarkedJob(@Field("job_id") jobId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("apply-Job")
    suspend fun applyJob(@Field("job_id") jobId: Int): Response<GeneralResponse>

    @POST("post-new-job-by-individual-job-giver")
    suspend fun postJobByIndividual(@Body model: PostNewJobByIndividualReq): Response<GeneralResponse>

    @POST("post-new-job-by-company")
    suspend fun postJobByCompany(@Body model: PostNewJobByCompany): Response<GeneralResponse>

    @POST("edit-job-post-as-individual")
    suspend fun editJobAsIndividual(@Body model: PostNewJobByIndividualReq): Response<GeneralResponse>

    @POST("edit-job-post-as-company")
    suspend fun editJobAsCompany(@Body model: PostNewJobByCompany): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("create-post-from-job")
    suspend fun createPostFromJob(@Field("job_id") jobId: Int): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("delete-job")
    suspend fun deleteJob(@Field("job_id") jobId: Int): Response<GeneralResponse>
}