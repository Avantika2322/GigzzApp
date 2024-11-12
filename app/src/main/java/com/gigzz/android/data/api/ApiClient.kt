package com.gigzz.android.data.api

import android.content.Context
import com.gigzz.android.utils.AppConstants
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object ApiClient {
    private val gson = GsonBuilder()
        .setLenient()
        .create()

   /* @Provides
    @Singleton
    fun create(): ApiInterface {

        val retrofit = Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create(gson)
            )
            .baseUrl(AppConstants.API_BASE_URL)
            .build()

        return retrofit.create(ApiInterface::class.java)
    }
*/
    @Provides
    @Singleton
    fun providesApiInterface(retrofitBuilder: Retrofit.Builder): ApiInterface {
        return retrofitBuilder.build().create(ApiInterface::class.java)
    }

    @Provides
    @Singleton
    fun contextProvider(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Provides
    @Singleton
    fun providesApiAuthInterface(
        retrofitBuilder: Retrofit.Builder,
        okHttpClient: OkHttpClient
    ): ApiAuthInterface {
        return retrofitBuilder.client(okHttpClient).build().create(ApiAuthInterface::class.java)
    }

    @Provides
    @Singleton
    fun providesRetrofitBuilder(): Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(AppConstants.API_BASE_URL)

    @Provides
    @Singleton
    fun providesOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            addNetworkInterceptor(authInterceptor)
        }.build()
    }
}