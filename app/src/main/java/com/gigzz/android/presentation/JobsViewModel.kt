package com.gigzz.android.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.common.emitErrorMessage
import com.gigzz.android.common.exceptionHandler
import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.data.repositories.JobsRepository
import com.gigzz.android.domain.req.AllJobsFilterReq
import com.gigzz.android.domain.req.CompanyJobsFilterReq
import com.gigzz.android.domain.req.IndividualJobsFilterReq
import com.gigzz.android.domain.req.PostNewJobByCompany
import com.gigzz.android.domain.req.PostNewJobByIndividualReq
import com.gigzz.android.domain.res.CompanyJobData
import com.gigzz.android.domain.res.GetAllCompanyPostedJobsRes
import com.gigzz.android.domain.res.GetJobsRes
import com.gigzz.android.domain.res.GetCompanyPostedJobByIdRes
import com.gigzz.android.domain.res.GetPostedJobByIdRes
import com.gigzz.android.domain.res.JobsData
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.dataStore
import com.gigzz.android.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val pref: PreferenceDataStoreModule,
    private val repo: JobsRepository
) : ViewModel() {
    val individualJobList: MutableList<JobsData> = mutableListOf()
    val companyJobList: MutableList<CompanyJobData> = mutableListOf()
    var editedJobId=-1
    var selectedJobTypeApplied=-1
    var selectedGigzz: String? = null

    private val _getAllJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val getAllJobsRes: LiveData<Resource<GetJobsRes>> = _getAllJobsRes

    private val _getAllIndividualJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val getAllIndividualJobsRes: LiveData<Resource<GetJobsRes>> = _getAllIndividualJobsRes

    private val _getAllCompanyJobsRes = MutableLiveData<Resource<GetAllCompanyPostedJobsRes>>()
    val getAllCompanyJobsRes: LiveData<Resource<GetAllCompanyPostedJobsRes>> = _getAllCompanyJobsRes

    private val _getPostedJobByIdRes = MutableLiveData<Resource<GetPostedJobByIdRes>>()
    val getPostedJobByIdRes: LiveData<Resource<GetPostedJobByIdRes>> = _getPostedJobByIdRes

    private val _getCompanyPostedJobByIdRes = MutableLiveData<Resource<GetCompanyPostedJobByIdRes>>()
    val getCompanyPostedJobByIdRes: LiveData<Resource<GetCompanyPostedJobByIdRes>> = _getCompanyPostedJobByIdRes

    private val _getAllBookmarkedJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val getAllBookmarkedJobsRes: LiveData<Resource<GetJobsRes>> = _getAllBookmarkedJobsRes

    private val _addJobToWishlistRes = MutableLiveData<Resource<GeneralResponse>>()

    private val _getMyAppliedJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val getMyAppliedJobsRes: LiveData<Resource<GetJobsRes>> = _getMyAppliedJobsRes

    /*private val _filterAppliedJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val filterAppliedJobsRes: LiveData<Resource<GetJobsRes>> = _filterAppliedJobsRes

    private val _filterCompanyJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val filterCompanyJobsRes: LiveData<Resource<GetJobsRes>> = _filterCompanyJobsRes

    private val _filterIndividualJobsRes = MutableLiveData<Resource<GetJobsRes>>()
    val filterIndividualJobsRes: LiveData<Resource<GetJobsRes>> = _filterIndividualJobsRes*/

    private val _applyJobRes = MutableLiveData<Resource<GeneralResponse>>()
    val applyJobRes: LiveData<Resource<GeneralResponse>> = _applyJobRes

    private val _postEditJobRes = MutableLiveData<Resource<GeneralResponse>>()
    val postEditJobRes: LiveData<Resource<GeneralResponse>> = _postEditJobRes

    private val _deleteJobRes = MutableLiveData<Resource<GeneralResponse>>()
    val deleteJobRes: LiveData<Resource<GeneralResponse>> = _deleteJobRes

    var filterAllJobsFilterReq = AllJobsFilterReq()
    var filterIndividualJobsReq = IndividualJobsFilterReq()
    var filterCompanyJobsReq = CompanyJobsFilterReq()

    suspend fun getProfileRes() =
        pref.appCtx.dataStore.data.first()

    fun getAllJobs(pageNumber: Int, jobType:Int) {
        _getAllJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllJobsRes)) {
                val response = repo.getMyAllJobsApi(pageNo = pageNumber, jobType =  jobType)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllJobsRes.postValue(Resource.InternetError())
        }
    }

    fun filterAllJobs() {
        _getAllJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllJobsRes)) {
                val response = repo.allJobFilterApi(filterAllJobsFilterReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllJobsRes.postValue(Resource.InternetError())
        }
    }

    fun getMyAppliedJobs() {
        _getMyAppliedJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getMyAppliedJobsRes)) {
                val response = repo.getMyAppliedJobsApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getMyAppliedJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getMyAppliedJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getMyAppliedJobsRes.postValue(Resource.InternetError())
        }
    }

    fun filterAppliedJobs(status: Int) {
        _getMyAppliedJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getMyAppliedJobsRes)) {
                val response = repo.myAppliedJobFilterApi(status)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    // getMyAppliedJobs()
                    _getMyAppliedJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getMyAppliedJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getMyAppliedJobsRes.postValue(Resource.InternetError())
        }
    }

    fun getAllIndividualJobs() {
        _getAllIndividualJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllIndividualJobsRes)) {
                val response = repo.getAllIndividualJobsApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    individualJobList.clear()
                    individualJobList.addAll(result.data)
                    _getAllIndividualJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllIndividualJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllIndividualJobsRes.postValue(Resource.InternetError())
        }
    }

    fun filterIndividualJobs() {
        _getAllIndividualJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllIndividualJobsRes)) {
                val response = repo.individualJobFilterApi(filterIndividualJobsReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllIndividualJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllIndividualJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllIndividualJobsRes.postValue(Resource.InternetError())
        }
    }

    fun getAllCompanyJobs() {
        _getAllCompanyJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllCompanyJobsRes)) {
                val response = repo.getAllCompanyJobsApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    val auth= pref.getFirstPreference(PrefKeys.AUTH_KEY, "")
                    companyJobList.clear()
                    companyJobList.addAll(result.data)
                    _getAllCompanyJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllCompanyJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllCompanyJobsRes.postValue(Resource.InternetError())
        }
    }

    fun filterCompanyJobs() {
        _getAllCompanyJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllCompanyJobsRes)) {
                val response = repo.companyJobFilterApi(filterCompanyJobsReq)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllCompanyJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllCompanyJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllCompanyJobsRes.postValue(Resource.InternetError())
        }
    }


    fun getMyBookmarkedJobs() {
        _getAllBookmarkedJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllBookmarkedJobsRes)) {
                val response = repo.getMyBookmarkedJobsApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllBookmarkedJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllBookmarkedJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllBookmarkedJobsRes.postValue(Resource.InternetError())
        }
    }

    fun bookmarkJobApi(jobId: Int) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_addJobToWishlistRes)) {
                repo.bookmarkJobApi(jobId)
                getMyBookmarkedJobs()
            }
        } else {
            _addJobToWishlistRes.postValue(Resource.InternetError())
        }
    }

    fun removeBookmarkJobApi(jobId: Int, from:String) {
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_addJobToWishlistRes)) {
                repo.removeBookmarkedJobApi(jobId)
                if (from=="allJobs") getMyBookmarkedJobs()
                else editedJobId=jobId
            }
        } else {
            _addJobToWishlistRes.postValue(Resource.InternetError())
        }
    }

    fun applyJobApi(jobId: Int) {
        _applyJobRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_applyJobRes)) {
                val response = repo.applyJobApi(jobId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _applyJobRes.postValue(Resource.Success(data = result))
                } else {
                    _applyJobRes.emitErrorMessage(response)
                }
            }
        } else {
            _applyJobRes.postValue(Resource.InternetError())
        }
    }

    fun getAllPostedJobs(jobType: Int,pageNumber: Int) {
        _getAllJobsRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAllJobsRes)) {
                val response = repo.getPostedJobsApi(jobType,pageNumber)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getAllJobsRes.postValue(Resource.Success(data = result))
                } else {
                    _getAllJobsRes.emitErrorMessage(response)
                }
            }
        } else {
            _getAllJobsRes.postValue(Resource.InternetError())
        }
    }


    fun getPostedJobById(jobId: Int) {
        _getPostedJobByIdRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getPostedJobByIdRes)) {
                val response = repo.postedJobByIdApi(jobId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getPostedJobByIdRes.postValue(Resource.Success(data = result))
                } else {
                    _getPostedJobByIdRes.emitErrorMessage(response)
                }
            }
        } else {
            _getPostedJobByIdRes.postValue(Resource.InternetError())
        }
    }

    fun getCompanyPostedJobById(jobId: Int) {
        _getCompanyPostedJobByIdRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getCompanyPostedJobByIdRes)) {
                val response = repo.postedCompanyJobByIdApi(jobId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _getCompanyPostedJobByIdRes.postValue(Resource.Success(data = result))
                } else {
                    _getCompanyPostedJobByIdRes.emitErrorMessage(response)
                }
            }
        } else {
            _getCompanyPostedJobByIdRes.postValue(Resource.InternetError())
        }
    }

    fun postNewJobByIndividual(model: PostNewJobByIndividualReq) {
        _postEditJobRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_postEditJobRes)) {
                val response = repo.postJobByIndividualApi(model)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                     getAllIndividualJobs()
                    _postEditJobRes.postValue(Resource.Success(data = result))
                } else {
                    _postEditJobRes.emitErrorMessage(response)
                }
            }
        } else {
            _postEditJobRes.postValue(Resource.InternetError())
        }
    }

    fun editJobAsIndividual(model: PostNewJobByIndividualReq) {
        _postEditJobRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_postEditJobRes)) {
                val response = repo.editJobAsIndividualApi(model)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getPostedJobById(model.job_id!!)
                    getAllIndividualJobs()
                    _postEditJobRes.postValue(Resource.Success(data = result))
                } else {
                    _postEditJobRes.emitErrorMessage(response)
                }
            }
        } else {
            _postEditJobRes.postValue(Resource.InternetError())
        }
    }

    fun postNewJobByCompany(model: PostNewJobByCompany) {
        _postEditJobRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_postEditJobRes)) {
                val response = repo.postJobByCompanyApi(model)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getAllCompanyJobs()
                    _postEditJobRes.postValue(Resource.Success(data = result))
                } else {
                    _postEditJobRes.emitErrorMessage(response)
                }
            }
        } else {
            _postEditJobRes.postValue(Resource.InternetError())
        }
    }

    fun editJobAsCompany(model: PostNewJobByCompany) {
        _postEditJobRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_postEditJobRes)) {
                val response = repo.editJobAsCompanyApi(model)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    getCompanyPostedJobById(model.job_id!!)
                    getAllCompanyJobs()
                    _postEditJobRes.postValue(Resource.Success(data = result))
                } else {
                    _postEditJobRes.emitErrorMessage(response)
                }
            }
        } else {
            _postEditJobRes.postValue(Resource.InternetError())
        }
    }

    fun createPostFromJob(jobId: Int) {
        _postEditJobRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_postEditJobRes)) {
                val response = repo.createPostFromJobApi(jobId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    _postEditJobRes.postValue(Resource.Success(data = result))
                } else {
                    _postEditJobRes.emitErrorMessage(response)
                }
            }
        } else {
            _postEditJobRes.postValue(Resource.InternetError())
        }
    }

    fun deleteJob(jobId: Int) {
        _deleteJobRes.postValue(Resource.Loading())
        if (isNetworkAvailable(pref.appCtx)) {
            viewModelScope.launch(exceptionHandler(_deleteJobRes)) {
                val response = repo.deleteJobApi(jobId)
                val result = response.body()
                if (response.isSuccessful && result != null) {
                     editedJobId=jobId
                    _deleteJobRes.postValue(Resource.Success(data = result))
                } else {
                    _deleteJobRes.emitErrorMessage(response)
                }
            }
        } else {
            _deleteJobRes.postValue(Resource.InternetError())
        }
    }


    fun clearEditPostJobRes(){
        _postEditJobRes.value=null
        _getPostedJobByIdRes.value=null
    }

    fun clearApplyJobRes(){
        _applyJobRes.value=null
    }
}