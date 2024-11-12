package com.gigzz.android.data.repositories

import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.data.api.ApiAuthInterface
import com.gigzz.android.domain.req.AllJobsFilterReq
import com.gigzz.android.domain.req.CompanyJobsFilterReq
import com.gigzz.android.domain.req.IndividualJobsFilterReq
import com.gigzz.android.domain.req.PostNewJobByCompany
import com.gigzz.android.domain.req.PostNewJobByIndividualReq
import com.gigzz.android.domain.res.GetAllCompanyPostedJobsRes
import com.gigzz.android.domain.res.GetJobsRes
import com.gigzz.android.domain.res.GetCompanyPostedJobByIdRes
import com.gigzz.android.domain.res.GetPostedJobByIdRes
import retrofit2.Response
import javax.inject.Inject

class JobsRepository @Inject constructor(
    private val apiAuthInterface: ApiAuthInterface
) {
    suspend fun getMyAllJobsApi(pageNo:Int,jobType: Int): Response<GetJobsRes> =
        apiAuthInterface.getAllJobs(pageNo = pageNo,jobType=jobType)

    suspend fun getMyAppliedJobsApi(): Response<GetJobsRes> =
        apiAuthInterface.getAppliedJobs()

    suspend fun getMyBookmarkedJobsApi(): Response<GetJobsRes> =
        apiAuthInterface.getBookmarkedJobs()

    suspend fun getAllIndividualJobsApi(): Response<GetJobsRes> =
        apiAuthInterface.getAllIndividualJobs()

    suspend fun getAllCompanyJobsApi(): Response<GetAllCompanyPostedJobsRes> =
        apiAuthInterface.getAllCompanyJobs()

    suspend fun companyJobFilterApi(model: CompanyJobsFilterReq): Response<GetAllCompanyPostedJobsRes> =
        apiAuthInterface.companyJobFilter(model)

    suspend fun individualJobFilterApi(model: IndividualJobsFilterReq): Response<GetJobsRes> =
        apiAuthInterface.individualJobFilter(model)

    suspend fun myAppliedJobFilterApi(searchStatus: Int): Response<GetJobsRes> =
        apiAuthInterface.getMyAppliedJobFilter(searchStatus)

    suspend fun allJobFilterApi(model: AllJobsFilterReq): Response<GetJobsRes> =
        apiAuthInterface.allJobsFilter(model)

    suspend fun getPostedJobsApi(jobType: Int, pageNo: Int): Response<GetJobsRes> =
        apiAuthInterface.getPostedJobs(jobType,pageNo)

    suspend fun postedJobByIdApi(jobId: Int): Response<GetPostedJobByIdRes> =
        apiAuthInterface.getPostedJobById(jobId)

    suspend fun postedCompanyJobByIdApi(jobId: Int): Response<GetCompanyPostedJobByIdRes> =
        apiAuthInterface.getCompanyPostedJobById(jobId)

    suspend fun bookmarkJobApi(jobId: Int): Response<GeneralResponse> =
        apiAuthInterface.bookmarkJob(jobId)

    suspend fun removeBookmarkedJobApi(jobId: Int): Response<GeneralResponse> =
        apiAuthInterface.removeBookmarkedJob(jobId)

    suspend fun applyJobApi(jobId: Int): Response<GeneralResponse> =
        apiAuthInterface.applyJob(jobId)

    suspend fun postJobByIndividualApi(model: PostNewJobByIndividualReq): Response<GeneralResponse> =
        apiAuthInterface.postJobByIndividual(model)

    suspend fun postJobByCompanyApi(model: PostNewJobByCompany): Response<GeneralResponse> =
        apiAuthInterface.postJobByCompany(model)

    suspend fun editJobAsIndividualApi(model: PostNewJobByIndividualReq): Response<GeneralResponse> =
        apiAuthInterface.editJobAsIndividual(model)

    suspend fun editJobAsCompanyApi(model: PostNewJobByCompany): Response<GeneralResponse> =
        apiAuthInterface.editJobAsCompany(model)

    suspend fun createPostFromJobApi(jobId: Int): Response<GeneralResponse> =
        apiAuthInterface.createPostFromJob(jobId)

    suspend fun deleteJobApi(jobId: Int): Response<GeneralResponse> =
        apiAuthInterface.deleteJob(jobId)
}