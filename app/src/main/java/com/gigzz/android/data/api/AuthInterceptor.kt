package com.gigzz.android.data.api

import com.gigzz.android.data.preference.PrefKeys
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    @Inject
    lateinit var store: PreferenceDataStoreModule

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val authKey = runBlocking { store.getFirstPreference(PrefKeys.AUTH_KEY, "") }
        request.addHeader("auth_key", authKey)
        return chain.proceed(request.build())
    }

}