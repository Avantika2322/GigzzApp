package com.gigzz.android.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.common.emitErrorMessage
import com.gigzz.android.common.exceptionHandler
import com.gigzz.android.data.api.ApiInterface
import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.data.repositories.SplashRepository
import com.gigzz.android.domain.res.GetSettingsResponse
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.isNetworkAvailable
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preference: PreferenceDataStoreModule,
    private val splashRepository: SplashRepository
) : ViewModel() {

    private var _isLoggedInLiveData = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedInLiveData
    private val _getSettingResponse = MutableLiveData<Resource<GetSettingsResponse>>()
    val getSettingResponse: LiveData<Resource<GetSettingsResponse>> = _getSettingResponse

    init {
        viewModelScope.launch {
            delay(1500)
            _isLoggedInLiveData.postValue(
                preference.getFirstPreference(PrefKeys.AUTH_KEY, "").isNotEmpty()
            )
        }
    }

    fun getToken() {
        viewModelScope.launch(Dispatchers.Default){
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("Device token", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
               // preferenceModule.deviceToken = token
                viewModelScope.launch {
                    preference.putPreference(PrefKeys.DEVICE_TOKEN, token)
                }
                Log.d("Device token", token)
            })
        }
    }

    fun getAppSetting() {
        if (isNetworkAvailable(preference.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getSettingResponse)) {
                val response = splashRepository.getSettingsApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    result.data.let {
                        preference.putPreference(PrefKeys.ACCESS_KEY, it[0].settingValue.toString())
                        preference.putPreference(PrefKeys.SECRET_KEY,it[2].settingValue.toString())
                        preference.putPreference(PrefKeys.BUCKET_NAME,it[1].settingValue.toString())
                    }
                    _getSettingResponse.postValue(Resource.Success(data = result))
                } else {
                    _getSettingResponse.emitErrorMessage(response)
                    /*val error =
                        Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    _getSettingResponse.postValue(Resource.Error(error.message, response.code()))*/
                }
            }
        } else {
            _getSettingResponse.postValue(Resource.InternetError())
        }
    }

    /*
    private val _runningPlanResponse = MutableLiveData<Resource<GetRunningFastInfoRes>>()
    val runningPlanResponse: LiveData<Resource<GetRunningFastInfoRes>> = _runningPlanResponse
    suspend fun getRunningPlansApi(): Response<GetRunningFastInfoRes> =
        apiInterface.getRunningPlan(preferenceModule.getString(PrefKeys.AUTH_TOKEN))
    fun getAppSetting() {
        if (isNetworkAvailable(preferenceModule.appCtx)) {
            viewModelScope.launch(exceptionHandler(_getAppSettingResponse)) {
                val response = splashRepository.getAppSetting()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    result.data.let {
                        preferenceModule.saveString(PrefKeys.ACCESS_KEY,it.access_key)
                        preferenceModule.saveString(PrefKeys.SECRET_KEY,it.secret_key)
                        preferenceModule.saveString(PrefKeys.BUCKET_NAME,it.bucket_name)
                    }

                } else {
                    val error =
                        Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    _getAppSettingResponse.postValue(Resource.Error(error.message, response.code()))
                }

            }
        } else {
            _getAppSettingResponse.postValue(Resource.InternetError())
        }
    }

    fun getRunningPlanApi() {
        if (isNetworkAvailable(preferenceModule.appCtx)) {
            _runningPlanResponse.postValue(Resource.Loading(null))
            viewModelScope.launch(exceptionHandler(_runningPlanResponse)) {
                val response = dashboardRepository.getRunningPlansApi()
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    runningPlanInfo.clear()
                    runningPlanInfo.addAll(result.data)
                    isRunningFirstCalled = true
                    _runningPlanResponse.postValue(Resource.Success(result))
                } else {
                    _runningPlanResponse.emitErrorMessage(response)
                }
            }
        } else {
            _runningPlanResponse.postValue(Resource.InternetError())
        }
    }

    private fun runningPlansObserver() {
        viewModel.runningPlanResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.show()
                    if (!viewModel.isRunningFirstCalled) binding.dashboardView.hide() else resetTimerView()
                }

                is Resource.Success -> {
                    initData()
                    binding.progressBar.hide()
                    binding.dashboardView.show()
                }

                is Resource.Error -> {
                    binding.progressBar.remove()
                    when (it.code) {
                        401 -> {
                            //openDeleteBlockDialog("delete")
                            viewModel.clearStoreData()
                            val intent = Intent(requireContext(), GetStartedActivity::class.java)
                            intent.putExtra("block", "delete")
                            startActivity(intent)
                            requireActivity().finishAffinity()
                        }

                        405 -> {
                            viewModel.clearStoreData()
                            val intent = Intent(requireContext(), GetStartedActivity::class.java)
                            intent.putExtra("block", "block")
                            startActivity(intent)
                            requireActivity().finishAffinity()
                        }

                        else -> {
                            //binding.progressBar.remove()
                            showToast(requireContext(), it.message.toString())
                        }
                    }
                }

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    binding.dashboardView.hide()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> {}
            }
        }
    }

    //get pref single value
        suspend fun getApprovalStatus() = prefKeys.getFirstPreference(PrefKeys.APPROVAL_STATUS, "")
       //put pref single value
           viewModelScope.launch {
                prefKeys.putPreference(PrefKeys.DEVICE_TOKEN, token)
            }

           //put pref model
            prefKeys.appCtx.dataStore.updateData {
                        it.copy(userStaticData = result)
                    }
            prefKeys.appCtx.dataStore.updateData { data ->
                    data.copy(
                        authKey = it.authKey,
                        userProfile = it.userProfile,
                        userPref = it.userPref,
                        countryCode = it.countryCode,
                        phoneNo = it.phoneNo,
                        userType = it.userType,
                        profileCompAsManager = it.profileCompAsManager,
                        profileCompAsUser = it.profileCompAsUser,
                        managerProfile = it.managerProfile
                    )
                }

          // get model
         suspend fun getProfileRes() =
        preferenceModule.appCtx.dataStore.data.first()

    suspend fun getUserImg() =
        preferenceModule.appCtx.dataStore.data.first().managerProfile?.get(0)?.profile_pic_thumbnail

    suspend fun getManagerProfile() =
        preferenceModule.appCtx.dataStore.data.first().managerProfile?.get(0)

    */
}