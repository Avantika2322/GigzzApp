package com.gigzz.android.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.gigzz.android.common.GeneralResponse
import com.gigzz.android.common.emitErrorMessage
import com.gigzz.android.common.exceptionHandler
import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.data.repositories.AuthRepository
import com.gigzz.android.data.repositories.NotificationRepository
import com.gigzz.android.domain.Demo
import com.gigzz.android.domain.req.AddEditEducationReq
import com.gigzz.android.domain.req.DeviceInfo
import com.gigzz.android.domain.req.SignUpAsCompanyReq
import com.gigzz.android.domain.req.SignUpAsIndividualJobGiverReq
import com.gigzz.android.domain.req.SignUpAsJobSeeker
import com.gigzz.android.domain.res.GetMasterDataRes
import com.gigzz.android.domain.res.MasterData
import com.gigzz.android.domain.res.SendOtpRes
import com.gigzz.android.domain.res.SignInRes
import com.gigzz.android.ui.auth.individual.IndividualJobGiverProfileFragment
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.DeviceInfoModule
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.dataStore
import com.gigzz.android.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val preference: PreferenceDataStoreModule,
    private val authRepository: NotificationRepository
) : ViewModel() {

}